1. About this project

	This is a fork of androminion by Ankur Metah, originated from google code
	project "androminion", an android based	implementation of the game
	"Dominion" by Rio Grande Games.
	
	This fork is currently based on version r561 (2012-08-02) of the official
	project.
	
	Goals of this fork are:
	- A better User Interface, first for smartphones and later for tablets
	- Better AI players, possibly using a neural network
	- Possibly a separate server and multiplayer functionality
	- And of course some bug fixing and code commenting

	Licensed under LGPL: http://www.gnu.org/licenses/lgpl-3.0.html

2. Branching strategy

	------------------------------------------------------------------------------
	master:
	latest stable version, ready for production
	------------------------------------------------------------------------------
	^
	|
	------------------------------------------------------------------------------
	develop:
	contains changes in progress, may not be ready for production
	will be marged into master when in a stable, tested state
	------------------------------------------------------------------------------
	^    ^    ^    ^    ^    
	|    |    |    |    |    
	------------------------------------------------------------------------------
	topic branches (e.g. dev-ui or dev-bot):
	for working on a feature or bug fix, will then be merged into develop
	------------------------------------------------------------------------------
  
	See also:
	http://stackoverflow.com/questions/2428722/git-branch-strategy-for-small-dev-team

3. Import newest changes from Google Code SVN

	3.1 Prepare SVN synchronisation with SVN metadata
	(Do only once after cloning bitbucket repository)
	
	git checkout gcodesource
	git update-ref refs/remotes/trunk origin/gcodesource
	git svn init http://androminion.googlecode.com/svn/trunk/
	git svn fetch

	3.2 Copy newest changes in SVN to bitbucket
	(Do as often as you want to)
	
	git checkout gcodesource
	git svn fetch (or git svn rebase?)
	git push gcodesource

	The newest changes in SVN/Google Code should now be copied to the
	gcodesource branch from where they can in turn be mergen into the other
	branches (possibly develop).