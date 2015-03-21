(use 'overtone.live)
(use 'launchkey-mini.core)
(boot-launchkey-mini!)

(do
  (def lk (first launchkeymini-kons))

  (use 'launchkey-mini.mode)
  (enable-session-mode :default)

  (use 'launchkey-mini.plugins.beat)

  (defonce count-trig-id (trig-id))

  (defonce root-trg-bus (control-bus "global metronome pulse"))
  (defonce root-cnt-bus (control-bus "global metronome count"))
  (defonce beat-trg-bus (control-bus "beat pulse (fraction of root)"))
  (defonce beat-cnt-bus (control-bus "beat count"))

  (def BEAT-FRACTION "Number of global pulses per beat" 30)
  (def current-beat-fraction (atom BEAT-FRACTION))
  (update-current-beat lk :default 0)

  (require '[overtone.synth.timing :as timing])
  (def r-cnt (timing/counter :in-bus root-trg-bus :out-bus root-cnt-bus))
  (def r-trg (timing/trigger :rate 100 :in-bus root-trg-bus))
  (def b-cnt (timing/counter :in-bus beat-trg-bus :out-bus beat-cnt-bus))
  (def b-trg (timing/divider :div BEAT-FRACTION :in-bus root-trg-bus :out-bus beat-trg-bus))
  (defsynth get-beat [] (send-trig (in:kr beat-trg-bus) count-trig-id (+ (in:kr beat-cnt-bus) 1)))

  (add-beat-event-handlers lk 0 :default count-trig-id)

  ;;Adjust bpm
  (defn bpm+ [] (ctl b-trg :div (swap! current-beat-fraction dec)))
  (defn bpm- [] (ctl b-trg :div (swap! current-beat-fraction inc)))

  ; start beat display
  (def thebeat (get-beat))
)

(comment
  (kill thebeat) ; stop the beat
)
