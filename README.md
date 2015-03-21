# overtone.device.launchkey-mini [![Build Status](https://travis-ci.org/magicmonty/overtone.device.launchkey-mini.svg?branch=master)](https://travis-ci.org/magicmonty/overtone.device.launchkey-mini)

[![Clojars Project](http://clojars.org/overtone.device.launchkey-mini/latest-version.svg)](http://clojars.org/overtone.device.launchkey-mini)

A Clojure library designed to use the Novation Launchkey Mini with [Overtone](http://overtone.github.io)

This code is mainly based on the work of @josephwilk at https://github.com/josephwilk/overtone.device.launchpad


# Usage

## installation
```bash
# Install lein2
# https://github.com/technomancy/leiningen

$ lein new mynewproject

# add the following dependencies to mynewproject/project.clj
# [org.clojure/clojure "1.5.1"]
# [overtone "0.9.1"]
# [overtone.device.launchkey-mini "0.1.0"]

$ cd mynewproject
$ lein repl
```

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
(set-page-max-x 1) ; set max 2 horizontal pages (endless if not set)
(set-page-max-y 1) ; set max 2 horizontal pages (endless if not set)

(set-mode :sequencer) ; switches to the mode with the ID :sequencer
(set-mode :drumpad) ; switches to the mode with the ID :drumpad
```

In session mode you have an infinite virtual grid via the up/down/left/right keys.
New pages will be added automatically after the last page.
You can limit the page count manually by setting `(set-page-max-r <mode-id> <last page no>)`
resp. `(set-page-max-y <mode-id> <last page no>)`.

In session mode, the state of the pad-keys (on/off) will be remembered automatically by state.

In non-session mode the up/down/left- and right-keys are also bindable.
The state of the pads is not remembered but the pads can be bound individually.

## Bind keys

Bind a pad
```clojure
(bind :mode :0x0 #(kick))
```

Bind the round row-buttons
```clojure
(bind :mode :row1 #(hat3))
(bind :mode :row2 #(kick))
```

Bind a meta-key (no effect in session mode):
```clojure
(bind :default :up (fn [launchkeymini] (println launchkeymini)))
```

Bind the knobs:
```clojure
; no arguments
(bind :default :knob1 (fn [] (println "Knob 1 value changed")))

; 1 argument: float value between 0.0 an 1.0
(bind :default :knob2 (fn [val-f] (println (str "Knob 2: " val-f))))

; 2 arguments: float value between 0.0 an 1.0 and absolute value between 0 and 127
(bind :default :knob3 (fn [val-f val] (println (str "Knob 3: " val " / " val-f))))

; 3 arguments: device, float value between 0.0 an 1.0 and absolute value between 0 and 127
(bind :default :knob4 (fn [lk val-f val] (println (str "Knob 4 on " lk ": " val " / " val-f))))
```

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
[:LKMiniInControl <device-index> <current mode id> :grid-off]
```

### Row (Side) controls

Both row controls send the following event:
```clojure
[:LKMiniInControl <device-index> <current mode id> :side]
```

Which button was pressed can be determined via the `:id` or the `:row` in the event data.
