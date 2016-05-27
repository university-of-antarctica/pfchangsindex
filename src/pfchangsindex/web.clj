(ns pfchangsindex.web
  (:require [compojure.core :refer [defroutes GET]]
            [ring.adapter.jetty :as ring]
            [hiccup.page :as page]
            [clj-http.client :as client]
            [clojure.data.json :as json]))

(defn places-req-copy []
  (slurp "places-req-out.txt"))

(defn places-req []
   (json/read-str
    (:body (client/get "https://maps.googleapis.com/maps/api/place/nearbysearch/json?"
              {:query-params {"key" (slurp "src/pfchangsindex/places-api-key.txt")
                              "location" "35.523839,-82.537188"
                              "radius" "50000"
                              "keyword" "food"}}))))

(defn index []
  (page/html5
   [:head
    [:title "Hello World"]]
   [:body
    [:div {:id "content"} "Hello World"]]))


(defroutes routes
  (GET "/" [] (str (index) (places-req))))

(defn -main []
  (ring/run-jetty #'routes {:port 8080 :join? false}))

