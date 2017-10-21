(ns datawalk.core
  (:require [datawalk.datawalk :as w]
            [datawalk.print    :as pr]
            [datawalk.parse    :as ps]
            #_[clojure.string :as string]
            #_[clojure.tools.reader :as rdr]
            #_[clojure.tools.reader.reader-types :as rdrt]
            ))

;; Dependencies:
;; core
;;   datawalk (contains state)
;;   parse
;;   print


;; Notes:
;; Throughout: d = data
;; - Would use a customized clojure.main repl, but AFAIK it won't work w cljs.
;; - A key challenge for making this work in cljs is that generic user input
;;   isn't available in cljs. I brought it up in #clojurescript, and found the
;;   following resources:
;;   - https://clojurians.slack.com/archives/C03S1L9DN/p1508602683000025
;;     - discussion goes at least until 2:30pm
;;     - note especially: `:special-fns` get processed by clj
;;   - https://github.com/abiocljs (@mfikes)
;;   - https://github.com/potetm/tire-iron
;;   - https://www.google.com/search?q=IReplEnvOption
;; - Assumption: the order of (keys m) will be consistent during a session.
;;   Empirically, this seems to be true, although I wouldn't necessarily trust
;;   it in production code.
;; - AFAICT, special fns are generally defined by the repl, so it
;;   may not be possible to inject them for my own needs.
;;   - see https://github.com/bhauman/lein-figwheel/blob/bddbcc0fe326ea1e4aafb649a0b95c9113377611/sidecar/src/figwheel_sidecar/system.clj
;; - Other options:
;;   - I could just make a totally ordinary fn and make users use that at the
;;   repl -- ie just print it out, and then do
;;   user> (x b) ; backward
;;   user> (x 3) ; drill to item 3
;;   ...and so on.
;;   - I could make users run in a specific env, eg planck or node. Unfortunately,
;;     reading input seems to be wildly inconsistent across browsers (eg
;;     SpiderMonkey has `read-line`), so I couldn't even make something that
;;     worked in all browser repls :(
;;   - I could make users copy-paste over into clj :(

(def prompt ".dw.> ")

(def exit-command? #{w/exit w/exit-with-current})

(defn initialize [d]
  (w/reset-data! d)
  ;; Start with (empty) path to root
  (reset! w/paths {d []})
  (reset! w/saved {})
  (reset! w/the-past [])
  (reset! w/the-future [])
  )

(defn read-input
  ;; TODO
  "Get user input (at repl) -- later this needs to be generalized for both clj
  and the various cljs environments."
  []
  (flush)
  (let [input (read-line)]
    (println input)
    input))

(defn datawalk
  "Runs a small, self-contained REPL for exploring data."
  ;; Technically a PREL
  [d]
  (println "Exploring.\n")
  (initialize d)
  (loop [data d]
    (println "past:   " @w/the-past)
    (println "future: " @w/the-future)
    (println)
    (pr/print-data data)
    (print prompt)
    (let [in (read-input)
          f (ps/parse in)
          result (if f (f data) data)]
      ;; Maybe I don't even need to store it, although users might like that
      ;; (w/reset-data! result)
      (if (exit-command? f)
        result
        (do (swap! w/the-past conj result)
            (recur result)))
      )))

(comment

  (datawalk {:a 1 :b {:c #{2 3 4} :d 5 :e [6 7 {:f 8 :g {:h 9}}]}})
  )
