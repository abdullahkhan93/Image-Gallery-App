from pyfcm import FCMNotification
from firebase_admin import db

'''
This class is used for sending messages to groups
'''
class Message():

    def __init__(self,group_to_notify, message_title, message_body):
        self.push_service = FCMNotification(api_key="AAAAYIooHik:APA91bF6utyDUSgqYX0MgZKhRM3-76kWvBYNXK6NY3HNKtM45shnt8JHbsx0x2XtpE2yXmEtH7MA6lR0M5e9BJuyJ5sDG1c8cjEwCpLZzZv93jyFUOQhofC1oOoMm6iWvfjRSuBU5_3r")
        self.message_title = message_title
        self.message_body = message_body
        self.notify_ids = self.fetch_group_members(group_to_notify)

    def fetch_group_members(self, group_id):
        members_ref = db.reference("/groups/" + group_id + "/members")
        users_ref = db.reference("/users")
        notify_ids = [users_ref.child(x).child("notify_id").get() for x in members_ref.get().keys()]
        print(notify_ids)
        return notify_ids

    def send(self):
        return self.push_service.notify_multiple_devices(registration_ids=self.notify_ids, message_title=self.message_title, message_body=self.message_body)
