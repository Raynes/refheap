(ns refheap.views.legal
  (:use [noir.core            :only [defpage]]
        [refheap.views.common :only [layout]]))

(defn render-tos []
  (layout
   [:div.written
    [:p [:b "Terms of Service"]]
    [:p
     "Under no circumstances, including, but not limited to, negligence, shall"
     " RefHeap or its affiliates be liable to you for any damages of any kind"
     " (including, but not limited to, compensatory damages, lost profits, lost"
     " data or any form of special, incidental, indirect, consequential or punitive"
     " damages whether based on breach of contract or warranty, tort, product"
     " liability or otherwise) that result from the use of, or the inability to"
     " use, this site or RefHeap’s products or services. You specifically"
     " acknowledge and agree that RefHeap is not liable for any defamatory,"
     " offensive or illegal conduct of any user, customer, or representative."
     " If you are dissatisfied with any material or content on RefHeap’s web site,"
     " products, external communications, services, or with any of RefHeap’s terms"
     " and conditions, your sole and exclusive remedy is to discontinue using"
     " RefHeap and its web sites, products, and services."]]))

(defn render-privacy []
  (layout
   [:div.written
    [:p [:b "Privacy Policy"]]
    [:p
     "Internet use and communication is subject to attack, interception, loss and"
     " alteration. On whichever site you provide personal information, it is possible"
     " for a third party to view or later retrieve and/or modify that information."
     " You acknowledge and agree that RefHeap, its maintainers, owners, and/or"
     " employees shall not be responsible for any damages you may suffer as a result"
     " of the transmission or storage of confidential or sensitive information "
     " communicated over the Internet or otherwise, and that all such communications"
     " will be at your own risk. Furthermore, RefHeap shall not be liable for"
     " maintaining the confidentiality of any communications between RefHeap’s"
     " representatives, affiliates, partners, users, or customers."]]))

(defpage "/legal/tos"     [] (render-tos))
(defpage "/legal/privacy" [] (render-privacy))