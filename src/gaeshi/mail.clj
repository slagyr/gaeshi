(ns gaeshi.mail
  (:import
    [com.google.appengine.api.mail MailService$Message MailService$Attachment MailServiceFactory]))

(defn- as-coll [value]
  (if (coll? value)
    value
    [value]))

(defn to-attachment [values]
  (MailService$Attachment. (:filename values) (:data values)))

(defn to-message [values]
  (doto (MailService$Message.)
    (.setAttachments (map to-attachment (as-coll (:attachments values))))
    (.setBcc (as-coll (:bcc values)))
    (.setCc (as-coll (:cc values)))
    (.setHtmlBody (:html values))
    (.setReplyTo (:reply-to values))
    (.setSender (:from values))
    (.setSubject (:subject values))
    (.setTextBody (:text values))
    (.setTo (as-coll (:to values)))))

(def service-instance (atom nil))

(defn mail-service []
  (when (nil? @service-instance)
    (reset! service-instance (MailServiceFactory/getMailService)))
  @service-instance)

(defn send-mail [values]
  (let [message (to-message values)]
    (.send (mail-service) message)))

(defn send-mail-to-admins [values]
  (let [message (to-message values)]
    (.sendToAdmins (mail-service) message)))