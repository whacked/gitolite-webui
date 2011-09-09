(ns gitolite-webui.trammel-checks)

(defmacro non-nil-params [fn]
  "Checks that all input parameters to fn are not nil"
  `(every? (comp not nil?) ~(first (:arglists (meta (resolve fn))))))

(defmacro keys-not-empty [map & keys]
  "Checks that all given keys are not empty in map" 
  `(every? (comp not empty?) ((juxt ~@keys) ~map))
  )

