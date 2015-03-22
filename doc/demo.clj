(use 'overtone.live)
(use 'launchkey-mini.core)
(boot-launchkey-mini!)

(do
  (def lk (first launchkeymini-kons))

  (def tom-electro-s       (sample (freesound-path 108001)))
  (def sizzling-high-hat-s (sample (freesound-path 44859)))
  (def kick-s              (sample (freesound-path 777)))
  (def hip-hop-kick-s      (sample (freesound-path 131336)))
  (def clap-s              (sample (freesound-path 24786)))
  (def bell-s              (sample (freesound-path 173000)))
  (def snare-s             (sample (freesound-path 100397)))

  (def samples-set [tom-electro-s sizzling-high-hat-s hip-hop-kick-s clap-s bell-s snare-s])

  (use 'launchkey-mini.plugins.beat)
  (mk-sequencer-mode lk 0 :sequencer 16 samples-set)

  ; start beat display
  (def thebeat (get-beat))
)

(comment
  (kill thebeat) ; stop the beat
)
