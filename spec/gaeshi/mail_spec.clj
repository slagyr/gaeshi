(ns gaeshi.mail-spec
  (:use
    [speclj.core]
    [gaeshi.mail]
    [gaeshi.spec_helpers.mail])
  (:import
    [com.google.appengine.api.mail MailService]))

(describe "Mail"

  (it "converts a simple map into a message"
    (let [message (map->message {:bcc "bcc@test.com"
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
    (let [message (map->message {:bcc ["bcc1@test.com" "bcc2@test.com"]
                                 :cc ["cc1@test.com" "cc2@test.com"]
                                 :to ["to1@test.com" "to2@test.com"]})]

      (should= ["bcc1@test.com" "bcc2@test.com"] (.getBcc message))
      (should= ["cc1@test.com" "cc2@test.com"] (.getCc message))
      (should= ["to1@test.com" "to2@test.com"] (.getTo message))))

  (it "creates messages with attachments"
    (let [message (map->message {:attachments [{:filename "one.txt" :data (.getBytes "Hello")}]})
          attachments (.getAttachments message)]
      (should= 1 (count attachments))
      (should= "one.txt" (.getFileName (first attachments)))
      (should= "Hello" (String. (.getData (first attachments))))))

  (it "converts message back into maps"
    (let [message-map {:attachments [{:filename "one.txt" :data (.getBytes "Hello")}]
                       :bcc ["bcc@test.com"]
                       :cc ["cc@test.com"]
                       :html "<html/>"
                       :reply-to "reply-to@test.com"
                       :from "sender@test.com"
                       :subject "Test Subject"
                       :text "Some Text"
                       :to ["to@test.com"]}
          message (map->message message-map)
          result (message->map message)]
      (should= message-map result)))

  (it "the mail service is a singleton"
    (should-not= nil (mail-service))
    (should= (mail-service) (mail-service)))

  (context "with mock service"
    (with-fake-mail)

    (it "sends a message"
      (send-mail {:to "to@test.com" :from "from@test.com" :text "Howdy!"})
      (should= 1 (count (sent-messages)))
      (let [message (first (sent-messages))]
        (should= ["to@test.com"] (:to message))
        (should= "from@test.com" (:from message))
        (should= "Howdy!" (:text message))))

    (it "sends a message to admins"
      (send-mail-to-admins {:from "sender@test.com" :text "Oh noes!"})
      (should= 1 (count (sent-messages-to-admins)))
      (let [message (first (sent-messages-to-admins))]
        (should= "sender@test.com" (:from message))
        (should= "Oh noes!" (:text message))))

    )

  )
