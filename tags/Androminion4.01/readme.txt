1. About this project

	This is a fork of androminion by Ankur Metah, originated from google code
	project "androminion", an android based	implementation of the game
	"Dominion" by Rio Grande Games.
	
	This fork was originally based on version r561 (2012-08-02) of the
	official project and has since been updated up to r638 (2012-08-31).
	
	Goals of this fork are:
	- A better User Interface, first for smartphones and later for tablets
	  (done)
	- Better AI players, possibly using a neural network
	  (work in progress)
	- Possibly a separate server and multiplayer functionality
	  (later)
	- And of course some bug fixing and code commenting
	  (partly done)

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
	
		git checkout master
		git update-ref refs/remotes/trunk origin/master
		git svn init http://androminion.googlecode.com/svn/trunk/
		git svn fetch

	3.2 Copy newest changes in SVN to bitbucket
	
		git checkout master
		git svn fetch (or git svn rebase?)
		git push master

	3.3 Copy newest changes in git/bitbucket back to SVN
	
		git checkout master
		git svn dcommit
			--commit-url https://androminion.googlecode.com/svn/trunk/ 
			--username=google.user@gmail.com
	
	Merging seems only to work properly with the master branch, so this is
	used for synchronizing svn and git.