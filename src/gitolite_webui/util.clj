(ns gitolite-webui.util
  )

(defn file [& v]
  (java.io.File. (apply str (interpose "/" v))))

(defn split-lines [s]
  (.split s "\r\n|\n"))

;; below is revived exactly or near exactly
(defn blank? [s]
  [^String s]
  (every? (fn [^Character c] (Character/isWhitespace c)) s))

(defn as-str
  "Like clojure.core/str, but if an argument is a keyword or symbol,
  its name will be used instead of its literal representation.
 
  Example:
     (str :foo :bar)     ;;=> \":foo:bar\"
     (as-str :foo :bar)  ;;=> \"foobar\"
 
  Note that this does not apply to keywords or symbols nested within
  data structures; they will be rendered as with str.
 
  Example:
     (str {:foo :bar})     ;;=> \"{:foo :bar}\"
     (as-str {:foo :bar})  ;;=> \"{:foo :bar}\" "
  ([] "")
  ([x] (if (instance? clojure.lang.Named x)
         (name x)
         (str x)))
  ([x & ys]
     ((fn [^StringBuilder sb more]
        (if more
          (recur (. sb  (append (as-str (first more)))) (next more))
          (str sb)))
      (new StringBuilder ^String (as-str x)) ys)))

