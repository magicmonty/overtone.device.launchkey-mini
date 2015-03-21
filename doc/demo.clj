(use 'overtone.live)
(use 'launchkey-mini.core)
(boot-launchkey-mini!)

(do
  (def lk (first launchkeymini-kons))

  (use 'launchkey-mini.mode)
  (add-mode!           lk :sequencer)
  (enable-session-mode lk :sequencer)
  (set-page-max-x      lk :sequencer 1)
  (set-page-max-y      lk :sequencer 3)
  (set-mode            lk :sequencer)

  (defonce count-trig-id (trig-id))

  (defonce root-trg-bus (control-bus "global metronome pulse"))
  (defonce root-cnt-bus (control-bus "global metronome count"))
  (defonce beat-trg-bus (control-bus "beat pulse (fraction of root)"))
  (defonce beat-cnt-bus (control-bus "beat count"))

  (def BEAT-FRACTION "Number of global pulses per beat" 30)
  (def current-beat-fraction (atom BEAT-FRACTION))

  (require '[overtone.synth.timing :as timing])
  (def r-cnt (timing/counter :in-bus root-trg-bus :out-bus root-cnt-bus))
  (def r-trg (timing/trigger :rate 100 :in-bus root-trg-bus))
  (def b-cnt (timing/counter :in-bus beat-trg-bus :out-bus beat-cnt-bus))
  (def b-trg (timing/divider :div BEAT-FRACTION :in-bus root-trg-bus :out-bus beat-trg-bus))
  (defsynth get-beat [] (send-trig (in:kr beat-trg-bus) count-trig-id (+ (in:kr beat-cnt-bus) 1)))

  ;;Adjust bpm
  (defn bpm+ [] (ctl b-trg :div (swap! current-beat-fraction dec)))
  (defn bpm- [] (ctl b-trg :div (swap! current-beat-fraction inc)))

  (def tom-electro-s       (sample (freesound-path 108001)))
  (def sizzling-high-hat-s (sample (freesound-path 44859)))
  (def kick-s              (sample (freesound-path 777)))
  (def hip-hop-kick-s      (sample (freesound-path 131336)))
  (def clap-s              (sample (freesound-path 24786)))
  (def bell-s              (sample (freesound-path 173000)))
  (def snare-s             (sample (freesound-path 100397)))

  (def samples-set [tom-electro-s sizzling-high-hat-s hip-hop-kick-s clap-s bell-s snare-s])

  (use 'launchkey-mini.plugins.sequencer)
  (def lk-sequencer (mk-sequencer "launchkey-sequencer" samples-set 16 beat-cnt-bus beat-trg-bus 0))

  (use 'launchkey-mini.plugins.beat)
  (update-current-beat lk :sequencer 0)
  (add-beat-event-handlers lk 0 :sequencer count-trig-id)

  ; start beat display
  (def thebeat (get-beat))
)

(comment
  (kill thebeat) ; stop the beat
)
