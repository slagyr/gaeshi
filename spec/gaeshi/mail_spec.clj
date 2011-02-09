(ns gaeshi.mail-spec
  (:use
    [speclj.core]
    [gaeshi.mail])
  (:import
    [com.google.appengine.api.mail MailService]))

(deftype MockService [sends sends-to-admins]
  MailService
  (send [this message] (swap! sends conj message))
  (sendToAdmins [this message] (swap! sends-to-admins conj message)))

(describe "Mail"

  (it "converts a simple map into a message"
    (let [message (to-message {:bcc "bcc@test.com"
                               :cc "cc@test.com"
                               :html "<html/>"
                               :reply-to "reply-to@test.com"
                               :from "sender@test.com"
                               :subject "Test Subject"
                               :text "Some Text"
                               :to "to@test.com"})]
      (should= ["bcc@test.com"] (.getBcc message))
      (should= ["cc@test.com"] (.getCc message))
      (should= "<html/>" (.getHtmlBody message))
      (should= "reply-to@test.com" (.getReplyTo message))
      (should= "sender@test.com" (.getSender message))
      (should= "Test Subject" (.getSubject message))
      (should= "Some Text" (.getTextBody message))
      (should= ["to@test.com"] (.getTo message))))

  (it "creates message with multiple recipients"
    (let [message (to-message {:bcc ["bcc1@test.com" "bcc2@test.com"]
                               :cc ["cc1@test.com" "cc2@test.com"]
                               :to ["to1@test.com" "to2@test.com"]})]

      (should= ["bcc1@test.com" "bcc2@test.com"] (.getBcc message))
      (should= ["cc1@test.com" "cc2@test.com"] (.getCc message))
      (should= ["to1@test.com" "to2@test.com"] (.getTo message))))

  (it "creates messages with attachments"
    (let [message (to-message {:attachments [{:filename "one.txt" :data (.getBytes "Hello")}]})
          attachments (.getAttachments message)]
      (should= 1 (count attachments))
      (should= "one.txt" (.getFileName (first attachments)))
      (should= "Hello" (String. (.getData (first attachments))))))

  (it "the mail service is a singleton"
    (should-not= nil (mail-service))
    (should= (mail-service) (mail-service)))

  (context "with mock service"
    (with service (MockService. (atom []) (atom [])))
    (before (reset! service-instance @service))

    (it "sends a message"
      (send-mail {:to "to@test.com" :from "from@test.com" :text "Howdy!"})
      (should= 1 (count @(.sends @service)))
      (let [message (first @(.sends @service))]
        (should= ["to@test.com"] (.getTo message))
        (should= "from@test.com" (.getSender message))
        (should= "Howdy!" (.getTextBody message))))

    (it "sends a message to admins"
      (send-mail-to-admins {:from "sender@test.com" :text "Oh noes!"})
      (should= 1 (count @(.sends-to-admins @service)))
      (let [message (first @(.sends-to-admins @service))]
        (should= "sender@test.com" (.getSender message))
        (should= "Oh noes!" (.getTextBody message))))

    )

  )
