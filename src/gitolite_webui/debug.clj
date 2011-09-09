(ns gitolite-webui.debug)

(defmacro dbg
  [x]
  `(let [x# ~x] (println "dbg:" '~x "=" x#) x#))


