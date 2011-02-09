(ns gaeshi.mail
  (:import
    [com.google.appengine.api.mail MailService$Message MailService$Attachment MailServiceFactory]))

(defn- as-coll [value]
  (if (coll? value)
    value
    [value]))

(defn map->attachment [values]
  (MailService$Attachment. (:filename values) (:data values)))

(defn attachment->map [attachment]
  {:filename (.getFileName attachment)
   :data (.getData attachment)})

(defn map->message [values]
  (doto (MailService$Message.)
    (.setAttachments (map map->attachment (as-coll (:attachments values))))
    (.setBcc (as-coll (:bcc values)))
    (.setCc (as-coll (:cc values)))
    (.setHtmlBody (:html values))
    (.setReplyTo (:reply-to values))
    (.setSender (:from values))
    (.setSubject (:subject values))
    (.setTextBody (:text values))
    (.setTo (as-coll (:to values)))))

(defn message->map [message]
  {:attachments (map attachment->map (.getAttachments message))
   :bcc (as-coll (.getBcc message))
   :cc (as-coll (.getCc message))
   :html (.getHtmlBody message)
   :reply-to (.getReplyTo message)
   :from (.getSender message)
   :subject (.getSubject message)
   :text (.getTextBody message)
   :to (as-coll (.getTo message))})

(def service-instance (atom nil))

(defn mail-service []
  (when (nil? @service-instance)
    (reset! service-instance (MailServiceFactory/getMailService)))
  @service-instance)

(defn send-mail [values]
  (let [message (map->message values)]
    (.send (mail-service) message)))

(defn send-mail-to-admins [values]
  (let [message (map->message values)]
    (.sendToAdmins (mail-service) message)))