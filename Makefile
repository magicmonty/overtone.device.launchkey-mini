all:
	lein midje launchkey-mini.t-led

ci:
	sudo apt-get update
	sudo apt-get install supercollider

travis:
	lein2 midje launchkey-mini.t-led

