(ns clj_pd.core
  (:require [clojure.string :as s]))

(def path "ftest.pd")

(def canvas (slurp path))

(defn objs [canvas]
  (s/split canvas #";\n"))

(defn str-map->num [k m]
  (let [s (k m)
        num (read-string s)]
    (assoc m k num)))

(defn to-map [obj]
  (let [els (s/split obj #" ")
        keys [:hash :type :x :y :name]
        obj-map (zipmap keys els)]
    (->> obj-map (str-map->num :x) (str-map->num :y))))

(def test-obj (-> canvas objs second to-map))

(defn move [obj x y]
  (assoc obj :x x :y y))

(defn stringify [{:keys [hash type x y name]}]
  (let [order [hash type x y name]
        spaces (interpose " " order)
        strs (map str spaces)
        s (apply str strs)]
    (str s \; \newline)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def port 8000)
(def path "server.pd")

(def test-obj
  (array-map :canvas "pd-new" :type "obj" :x 10 :y 20 :name "osc~"))
(def test-msg
  (array-map :canvas "pd-new" :type "msg" :x 100 :y 100 :text "hello world"))
(def test-float
  (array-map :canvas "pd-new" :type "floatatom" :x 100 :y 100))
(def test-symbol
  (array-map :canvas "pd-new" :type "symbolatom" :x 350 :y 70 :symbol "symbol"))
(def test-text
  (array-map :canvas "pd-new" :type "text" :x 350 :y 130 :text "Hello World!"))
(def test-connection
  (array-map :canvas "pd-new" :type "connect" :orig-obj 0 :outlet 0 :dest-obj 1 :inlet 0))
(def test-graph
  (array-map :canvas "pd-new" :type "graph" :name "mygraph"))
;; add array to this

;;add args vector

(defn clear-canvas [canvas]
  (send-pd (str canvas " clear;")))

(defn parse-out [arrmap]
  (let [els (->> arrmap
                 vals
                 (interpose " ")
                 (map str)
                 (apply str))
        msg (str els \; \newline)]
    (send-pd msg)))

 (defn send-pd [msg]
     (let [command (format "echo '%s' | pdsend %d" msg port)]
       (clojure.java.shell/sh "bash" "-c" command)))

(defn run-pd [path]
  (future (clojure.java.shell/sh "pd" path)))
