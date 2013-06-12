(ns gitolite-webui.util
  )

(defn file [& v]
  (java.io.File. (apply str (interpose "/" v))))

