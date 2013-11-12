package com.vdom.comms;

import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


import com.vdom.comms.Event.EType;

/**
 * Ok, I reworked this class a little.
 * Originally, this was a class for a hybrid between asynchronous and event-driven communication.
 * One could communicate using public get and put asynchronously and / or run this class as a thread.
 * 
 * Now this class spawn a thread upon creation and communicate with it in a threadsafe manner.
 * Use doWait() in place of get(); put() keeps its functionality. 
 *
 */

public class Comms {
    final static int TIMEOUT = 15000; // 15 seconds in ms
    final static boolean DEBUGGING = false;

    String host;
    int port;
    EventHandler parent;

    private boolean isServer = true;


    public class MonitorObject{};

    private Socket pclient = null;
    private SocketThread networkThread;

    LinkedBlockingQueue<Event> latestEvents = new LinkedBlockingQueue<Event>();


    /*
     * The following functions (doWait, get_ts, poll, doWaitTimeout) are used to synchronously receive packets
     * in a thread-safe manner (unlike the horrible mess we had before). 
     * !!! They receive everything for which the message-handler returned false. !!!
     */


    public Event doWait(){
        return doWaitTimeout(TIMEOUT);
    }

    public Event get_ts() { // the old get, but threadsafe
        return doWaitTimeout(-1);
    }

    public Event poll() { // Look if we received something but don't block
        return doWaitTimeout(0);
    }

    /**
     * Returns the latest Event or null if none received within timeout
     * @param timeout in milliseconds. May be 0 if we don't wait (poll), or negative for infinite wait 
     */
    public Event doWaitTimeout(long timeout) {
        Event e = null;
        long towait = 0;
        long endtime = -1;
        boolean usetimeout = (timeout > 0);
        if (usetimeout) {
            endtime = System.nanoTime() + timeout * 1000 * 1000; // Don't use System.currentTimeMillis, it fails on timezone change etc.
        }

        if (networkThread.getDone()) // dispatch loop has finished
            return null;

        while(e == null){
            if (usetimeout) {
                towait = (endtime - System.nanoTime());
                if (towait <= 0) {
                    break;
                }
            } else {
                if (timeout == 0) { // return instantly
                    return latestEvents.poll();
                }
            }

            try{
                if (usetimeout) {
                    e = latestEvents.poll(towait, TimeUnit.NANOSECONDS);
                } else {
                    e = latestEvents.take(); // wait indefinitely
                }
            } catch(InterruptedException e1){

            }				

            if (e != null && e.t == EType.KILLSENDER) { // We need the possibility to cancel a get_ts in case we had a put_ts error.
                return null;
            }
            if (networkThread.getDone()) // dispatch loop has finished
                return null;


        }
        return e;
    }

    public void put_ts(Event e) {
        if (!networkThread.getDone())
            networkThread.put(e);
    }


    /**
     * Wait for the created thread to finish initializing, so that we know
     * if we want to throw an exception.
     * TODO: This is not the android-way of doing things, but it fits better into the existing codebase.
     */
    private void waitForThreadInit(long timeout) {
        synchronized (networkThread.exceptionMonitorObject) {
            long endtime = System.nanoTime() + timeout * 1000 * 1000;  // Don't use System.currentTimeMillis, it fails on timezone change etc.
            long towait = timeout;  // wait 2 seconds max
            while (!networkThread.socketThreadInitialized) {
                try {
                    networkThread.exceptionMonitorObject.wait(towait);
                } catch (InterruptedException e) {
                    // don't care really
                }
                towait = (endtime - System.nanoTime()) / (1000 * 1000);
                if (towait <= 0) {
                    break;
                }
            }
        }
    }


    /**
     * Initialize this as server.
     * 
     * This starts the network thread, which does the initialization
     * of the socket and starts listening.
     * This also waits until the network thread is done initializing and
     * throws an error. TODO: This could block if the socket initialization
     * takes long (does that happen?)
     * @param parent In the example it's the VDomServer object
     * @param port
     * @throws IOException
     */
    public Comms(EventHandler parent, int port) throws IOException {
        this.parent = parent;
        this.isServer = true;
        this.port = port;
        parent.debug("Creating server");
        networkThread = new SocketThread();

        new Thread(networkThread).start();

        waitForThreadInit(2000);

        if (networkThread.thrownIOException != null) { throw networkThread.thrownIOException; }
    }

    /**
     * Initialize as client
     * 
     * This starts the network thread, which does the initialization
     * of the socket and connects.
     * TODO: Do callers rely on the socket being connected as soon
     * as this returns?
     * @param parent (GameActivity)
     * @param host
     * @param port
     */
    public Comms(EventHandler parent, String host, int port) throws StreamCorruptedException, IOException, UnknownHostException {
        this.parent = parent;
        this.isServer = false;
        this.host = host;
        this.port = port;
        parent.debug("Creating client");
        networkThread = new SocketThread();

        new Thread(networkThread).start();

        waitForThreadInit(2000);

        if (networkThread.thrownUnknownHostException != null ) {throw networkThread.thrownUnknownHostException; }
        if (networkThread.thrownStreamCorruptedException != null)  {throw networkThread.thrownStreamCorruptedException; }
        if (networkThread.thrownIOException != null) { throw networkThread.thrownIOException; }
    }


    public String getHost() {
        return host;
    }
    public int getPort() {
        return port;
    }


    private class SocketThread implements Runnable {

        private ServerSocket pserver = null;

        private ObjectInputStream ois = null;
        private ObjectOutputStream oos = null;

        private volatile boolean done = false; // this is true if and only if the dispatchLoop is working

        /**
         * Exception can be UnknownHostException, IOException, or StreamCorruptedException 
         */
        public IOException thrownIOException = null; // this is set to something in case creating the server fails
        public UnknownHostException thrownUnknownHostException = null;
        public StreamCorruptedException thrownStreamCorruptedException = null;

        public boolean socketThreadInitialized = false;

        private MonitorObject exceptionMonitorObject = new MonitorObject();

        private SendingThread sendingThread = null;

        /**
         * Wake up threads waiting for us to receive something with get or doWait
         * @param e the event we received
         */
        private void doNotify(Event e){
            latestEvents.offer(e);
        }

        /**
         * doWait will return NULL once
         */
        public void injectNullReceived() {
            doNotify(new Event(EType.KILLSENDER));
        }

        /**
         * Set 'done' to true and wake up threads waiting for us to receive something. 
         */
        public void setDoneTrue() {
            done = true;
            injectNullReceived();
        }

        public boolean getDone() {
            return done;
        }

        /**
         * Check if we are currently connected
         * @return true if connected, false otherwise
         */
        private boolean isConnected() {
            return (pclient == null ? false : pclient.isConnected());
        }

        private void CreateServer() throws IOException {
            debug("Opening server socket...");

            pserver = new ServerSocket(port);
            host = pserver.getInetAddress().getHostAddress();
            debug("Opened: " + host + " / " + port);
        }

        public SocketThread()
        {

        }

        private void debug(String s) {
            s = host + ":" + port + " -- " + s;
            // System.err.println (":: Androminion :: " + s);
            if (DEBUGGING)
                parent.debug(s);
        }

        /**
         * This function blocks until it received something from the network.
         * This function should only be called from the network thread! 
         * @return the received object, casted to an Event
         * @throws IOException
         */

        private Event get() throws IOException {
            Event p = null;
            try {
                // debug("Trying to get...");
                p = (Event) ois.readObject();
                debug("Got: " + p.toString());
            } catch (OptionalDataException e) {
                debug("OptionalDataException in Comms.get() -- ignoring.");
            } catch (ClassNotFoundException e) {
                debug("ClassNotFoundException in Comms.get() -- ignoring.");
            } catch (NullPointerException e) {
                debug("NullPointerException in Comms.get() -- ignoring.");
            }

            return p;
        }

        /**
         * This function assumes that done is set to true.
         * @return
         */
        private boolean disconnect() {
            if (!done) {
                debug("Comms::SocketThread: 'disconnect' executed, but 'done' is not true.");
            }
            boolean clean = true;
            debug("Shutting down...");

            debug("Waiting for sendqueue to drain");
            put(new Event(EType.KILLSENDER));
            while (sendingThread != null && sendingThread.isAlive()) {
                try {
                    sendingThread.join();
                } catch (InterruptedException e) {
                    // go on waiting
                }
            }

            try {
                // close I/O streams
                pclient.shutdownInput();
                pclient.shutdownOutput();
                debug("Streams shutdown.");
            } catch (Exception e) {
                clean = false;
            }

            try {
                oos.close();
                ois.close();
                debug("Streams closed.");
            } catch (Exception e) {
                clean = false;
            }

            try {
                // close socket connection
                pclient.close();
                debug("Socket closed.");
            } catch (Exception e) {
                clean = false;
            }

            if (isServer) {
                // close server
                // debug ("Stopping server...");
                try {
                    pserver.close();
                    debug("Server stopped");
                } catch (Exception e) {
                    clean = false;
                }
            }

            ois = null;
            oos = null; 
            pclient = null;
            pserver = null;
            debug(clean? "Disconnected cleanly" : "No clean disconnect. Apparently was already partially disconnected.");
            return clean;
        }



        private LinkedBlockingQueue<Event> toSendQueue = new LinkedBlockingQueue<Event>();
        private class SendingThread extends Thread {
            @Override
            public void run() {
                Event toSend;
                while (true) {
                    try {
                        toSend = toSendQueue.take();
                    } catch (InterruptedException e) {
                        continue;
                    }
                    debug("sending event " + toSend.toString());
                    if (toSend.t == EType.KILLSENDER) {
                        break;
                    }
                    try {
                        oos.writeObject(toSend);
                    } catch (IOException e) {
                        if (!done) { // done is volatile, so this works
                            parent.sendErrorHandler(e);
                        }
                    }
                } // while (true)
                debug("sending thread dying");
            }
        }

        public void put(Event p) {
            debug("Put: " + p.toString());
            if (!toSendQueue.offer(p)) {
                debug("Send Queue is full. Since the capacity of the queue is MAX_VALUE, you will not see this.");
                System.exit(1); // TODO: let the user know in some nicer way that something is seriously broken
            }
        }

        private Event ping() {
            // the try-block is not necessary; if there is an error, we will notice
            // since we aren't receiving anything.
            //			try {
            put(new Event(Event.EType.PING));
            //			} catch (Exception e) {
            //				debug("Exception in Comms.ping() while sending -- quitting.");
            //				e.printStackTrace();
            //				return true;
            //			}

            Event p;

            try {
                p = get(); // This is legal, we are in the receiving thread
            } catch (SocketTimeoutException e) {
                debug("Timed out in Comms.ping() -- quitting.");
                return null;
            } catch (Exception e) {
                debug("Exception in Comms.ping() while recving -- quitting.");
                e.printStackTrace();
                return null;
            }

            if (p == null) {
                debug("Invalid packet in Comms.ping() -- quitting.");
            }
            return p;
        }


        private void connect() throws UnknownHostException, IOException, StreamCorruptedException {
            if (isConnected())
                return;

            // open a socket connection
            if (isServer)
                pclient = pserver.accept();
            else
                pclient = new Socket(host, port);

            // Set read timeout, double for servers than for clients
            pclient.setSoTimeout(TIMEOUT * (isServer ? 2 : 1)); 

            // open I/O streams for objects
            oos = new ObjectOutputStream(pclient.getOutputStream());
            ois = new ObjectInputStream(pclient.getInputStream());
            toSendQueue.clear();
            sendingThread = new SendingThread();
            sendingThread.start();
        }

        @Override
        public void run() {
            synchronized (exceptionMonitorObject) {
                try {
                    if (isServer) {
                        CreateServer();
                    } else {
                        try {
                            connect();
                        } catch (UnknownHostException e) {
                            thrownUnknownHostException = e;
                        } catch (StreamCorruptedException e) {
                            thrownStreamCorruptedException = e;
                        }
                    } 
                } catch (IOException e) {
                    thrownIOException = e;
                } finally {
                    socketThreadInitialized = true;
                    exceptionMonitorObject.notify();
                }
            }
            dispatchLoop();
        }

        private void dispatchLoop() {
            done = false;
            if (!isConnected()) {
                if (isServer) {
                    try {
                        connect();
                    } catch (Exception e) {
                        debug("Failed to connect in run: " + e.getMessage());

                        setDoneTrue();
                        return;
                    }
                } else {

                    setDoneTrue();
                    return;
                }
            }

            boolean timeout, disconnect = false;
            Event p = null;

            while (!done) {
                timeout = false;
                if (p == null) { // if p is not null, we handle p without receiving
                    try {
                        p = get();
                    } catch (SocketTimeoutException e) {
                        debug("Connection timed out...");
                        timeout = true;
                    } catch (EOFException e) {
                        debug("Socket externally closed in Comms.run() -- quitting.");
                        disconnect = true;
                    } catch (Exception e) {
                        debug("Other exception in Comms.run() -- quitting.");
                        e.printStackTrace();
                        disconnect = true;
                    }
                }

                if (done) break;
                if (p != null && p.t == EType.SLEEP) {
                    try { Thread.sleep(p.i); }
                    catch (InterruptedException e) {}
                    p = null;
                    continue;
                }	
                if ((p != null) && (p.t == Event.EType.PING))
                    try {
                        put(new Event(Event.EType.PONG));
                    } catch (Exception e) {
                        debug("Could not pong in Comms.run() -- quitting.");
                        e.printStackTrace();
                        disconnect = true;
                    }
                else if ((p != null) && (p.t == Event.EType.GETSERVER))
                    try {
                        put(new Event(Event.EType.SERVER).setString(host).setInteger(port));
                    } catch (Exception e) {
                        debug("Could not pong server in Comms.run() -- quitting.");
                        e.printStackTrace();
                        disconnect = true;
                    }
                else if (p != null) {
                    if (!parent.handle(p))
                        doNotify(p);
                }
                if (timeout) {  // If we timed out:
                    p = ping(); // send a ping.
                    if (p == null) { // if ping unsuccessful: disconnect
                        disconnect = true;
                    } else {
                        if (p.t == EType.PONG) { // if it is successful and we received PONG: delete p
                            p = null;			 // OTHERWISE: keep p for next round! This solves a race condition
                        }
                    }
                } else {
                    p = null;
                }

                if (disconnect) {
                    p = new Event(Event.EType.DISCONNECT);
                    if (!parent.handle(p))
                        doNotify(p);
                    setDoneTrue();
                }
            }

            disconnect();
            debug("End of Comms.run()");
        }
    }

    public boolean stop() {
        networkThread.setDoneTrue();
        return networkThread.disconnect();
    }

    public void injectNullReceived() {

        networkThread.injectNullReceived();
    }

}
