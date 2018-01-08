from threading import Timer
from firebase_admin import db
from message import Message
from resources import ResourceManager


class ExpireHandler():

    def __init__(self, group_id, time_alive):
        self.group_id = group_id
        self.time_alive = time_alive
        timer = Timer(self.time_alive, self.expire)
        timer.start()

    def expire(self):
        # get reference to group by id
        try:
            group_ref = db.reference("/groups/" + self.group_id)
            group_dict = group_ref.get()
            members_list = group_dict["members"].keys()
            # Prepare message
            message_title = "Group has expired!"
            message_body = "The group " + group_dict['groupname'] + " has expired."
            message = Message(self.group_id, message_title, message_body)
            # Create resource manager
            rm = ResourceManager(self.group_id)
            # Set the current group of everyone who was in this group to null
            for member in members_list:
                user_group_ref = db.reference("/users/" + member + "/current_group")
                user_group_ref.delete()
            #Clean all resources
            group_ref.delete()
            rm.delete_blobs()
            # Notify all users in group
            message.send()
        except Exception as ex:
            print(ex)
            return
