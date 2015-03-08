all:
	lein midje launchkey-mini.led-test

ci:
	sudo apt-get update
	sudo apt-get install supercollider

travis:
	lein2 midje launchkey-mini.led-test

