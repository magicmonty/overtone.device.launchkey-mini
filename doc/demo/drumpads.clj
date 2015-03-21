(do
  (use 'overtone.inst.drum)
  (use 'launchkey-mini.mode)

  ; add mode if mode does not exist
  (when-not (mode? :drums)
    (add-mode! :drums)
    (disable-session-mode :drums))

  ; bind first row
  (bind :drums :0x0 #(kick))
  (bind :drums :1x0 #(kick2))
  (bind :drums :2x0 #(dub-kick))
  (bind :drums :3x0 #(dance-kick))
  (bind :drums :4x0 #(dry-kick))
  (bind :drums :5x0 #(quick-kick))
  (bind :drums :6x0 #(haziti-clap))
  (bind :drums :7x0 #(bing))

  ; bind second row
  (bind :drums :0x1 #(open-hat))
  (bind :drums :1x1 #(closed-hat))
  (bind :drums :2x1 #(closed-hat2))
  (bind :drums :3x1 #(hat3))
  (bind :drums :4x1 #(soft-hat))
  (bind :drums :5x1 #(snare))
  (bind :drums :6x1 #(snare2))
  (bind :drums :7x1 #(noise-snare))

  ; set mode
  (set-mode :drums))
