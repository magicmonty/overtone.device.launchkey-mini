# overtone.device.launchkey-mini

[![Build Status](https://travis-ci.org/magicmonty/overtone.device.launchkey-mini.svg?branch=master)](https://travis-ci.org/magicmonty/overtone.device.launchkey-mini)

A Clojure library designed to use the Novation Launchkey Mini with [Overtone](http://overtone.github.io)

This code is mainly based on the work of @josephwilk at https://github.com/josephwilk/overtone.device.launchpad


# Usage

## Startup

```clojure
(use 'overtone.live)
(use 'launchkey-mini.core)
(boot-launchkey-mini!)
```

## Modes
```clojure
(use 'launchkey-mini.mode)

(add-mode! :drumpads) ; adds a new mode
(disable-session-mode :drumpads) ; disables session mode and makes the pads bindable

(add-mode! :sequencer) ; adds a new mode
(enable-session-mode :sequencer) ; enables session mode and makes the pads pageable

(set-mode :sequencer) ; switches to the mode with the ID :sequencer
(set-mode :drumpad) ; switches to the mode with the ID :drumpad
```

In session mode you have an infinite virtual grid via the up/down/left/right keys.
New pages will be added automatically after the last page. In session mode,
the state of the pad-keys (on/off) will be remembered automatically by state.

In non-session mode the up/down/left- and right-keys are also bindable.
The state of the pads is not remembered but the pads can be bound individually.

## Bind keys

* Bind a pad `(bind :mode :0x0 #(kick))`
* Bind a round row-button `(bind :mode :row1 #(hat3))` or `(bind :mode :row2 #(kick))`
* Bind a meta-key (not useable in session mode): `(bind :default :up (fn [launchkeymini] (println launchkeymini)))`

## Events

### Meta-keys

The Meta-Keys send multiple events:

Up-Key pressed:
```clojure
[:LKMiniInControl <device-index> <current mode id> :meta :up-on]
[:LKMiniInControl <device-index> <current mode id> :meta :up]
```

Up-Key released:
```clojure
[:LKMiniInControl <device-index> <current mode id> :meta :up-off]
[:LKMiniInControl <device-index> <current mode id> :meta :up]
```

Down-Key pressed:
```clojure
[:LKMiniInControl <device-index> <current mode id> :meta :down-on]
[:LKMiniInControl <device-index> <current mode id> :meta :down]
```

Down-Key released:
```clojure
[:LKMiniInControl <device-index> <current mode id> :meta :down-off]
[:LKMiniInControl <device-index> <current mode id> :meta :down]
```

Left-Key pressed:
```clojure
[:LKMiniInControl <device-index> <current mode id> :meta :left-on]
[:LKMiniInControl <device-index> <current mode id> :meta :left]
```

Left-Key released:
```clojure
[:LKMiniInControl <device-index> <current mode id> :meta :left-off]
[:LKMiniInControl <device-index> <current mode id> :meta :left]
```

Right-Key pressed:
```clojure
[:LKMiniInControl <device-index> <current mode id> :meta :right-on]
[:LKMiniInControl <device-index> <current mode id> :meta :right]
```

Right-Key released:
```clojure
[:LKMiniInControl <device-index> <current mode id> :meta :right-off]
[:LKMiniInControl <device-index> <current mode id> :meta :right]
```

### Pads

Pad pressed:
```clojure
[:LKMiniInControl <device-index> <current mode id> :grid-on]
```

Pad released:
```clojure
[:LKMiniInControl <device-index> <current mode id> :grid-of]
```
