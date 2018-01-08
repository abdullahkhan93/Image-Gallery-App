from flask import Flask, jsonify, make_response
from flask import request
import firebase_admin
from firebase_admin import db
from firebase_admin import auth
import secrets
from message import Message
from expire import ExpireHandler
from resources import ResourceManager
import time

firebase_admin.initialize_app(None, { 'databaseURL' : 'https://mcc-fall-2017-g03.firebaseio.com/'})

app = Flask(__name__)

@app.route("/api/updatenotifyid", methods=["PUT"])
def update_notify_id():
    data = request.headers
    # verify users id token
    id_token = data['Authorization']
    try:
        decoded_token = auth.verify_id_token(id_token)
    except Exception as ex:
        return make_response('user not found', 401)
    # get values from dictionary
    uid = decoded_token["uid"]
    notify_id = data["notify_id"]
    try:
        user_ref = db.reference("/users/" + uid)
        user_ref.update({ "notify_id" : notify_id })
        return make_response("notify id updated", 201)
    except Exception as ex:
        return make_response('Updating notify id failed', 500)


#API endpoint for creating groups
# Looks for the following headers in the request:
# Authorization: Verification token which the client can request from Firebase
# group_name: Name of the group
# duration: Time which the group will remain active in hours
@app.route("/api/creategroup", methods=["POST"])
def create_group():
    #get headers
    data = request.headers
    #verify users id token
    id_token = data['Authorization']
    try:
        decoded_token = auth.verify_id_token(id_token)
    except Exception as ex:
        return make_response('user not found', 401)
    #get values from dictionary
    uid = decoded_token["uid"]
    group_name = data["group_name"]
    duration = int(data["duration"])
    #get user information
    user = decoded_token["name"]
    #create single use token for group
    try:
        #get reference to database and add a new group entry
        groups_ref = db.reference("/groups")
        new_group_ref = groups_ref.push()
        new_group_ref.set({
            'groupname': group_name,
            'duration': duration,
            'owner': {
                'id': uid,
                'name': user
            },
            'members': {
                uid: {
                    'name': user
                }
            }
        })
        #Update users current group
        user_ref = db.reference("/users/" + uid)
        user_ref.update({ "current_group": new_group_ref.key })
        # set token
        group_ref = db.reference("/groups/"+new_group_ref.key)
        group_ref.update({ "token": create_group_token(new_group_ref.key)})
        #Set expiration handler
        #Time in seconds
        time_alive = int((duration - int(round(time.time() * 1000)))/1000)
        ExpireHandler(new_group_ref.key, time_alive)
        # return the group in json format
        response_dict = {"group_id": new_group_ref.key, "token": new_group_ref.child('token').get()}
        return make_response(jsonify(response_dict), 201)
    except Exception as ex:
        print(ex)
        return make_response('Creating group unsuccessful', 500)


#API endpoint for joining groups
# Looks for the following headers in the request:
# Authorization: Verification token which the client can request from Firebase
# token: Single use token for joining the group
@app.route("/api/joingroup", methods=["PUT"])
def join_group():
    #get headers
    data = request.headers
    #verify users id token
    id_token = data['Authorization']
    try:
        decoded_token = auth.verify_id_token(id_token)
    except Exception as ex:
        return make_response('user not found', 401)
    #get values from dictionary
    group_id = parse_group_id(data["token"])
    token = data["token"]
    #get user information
    uid = decoded_token["uid"]
    user = decoded_token["name"]
    #get reference to group by id
    try:
        group_ref = db.reference("/groups/" + group_id)
        group_dict = group_ref.get()
        current_token = group_dict['token']
        if uid not in group_dict['members'] and token == current_token:
            # Prepare message
            message_title = "New group member!"
            message_body = "Your group " + group_dict['groupname'] + " has a new member: " + user
            message = Message(group_id, message_title, message_body)
            group_ref.child('members').update({
                uid: {
                    'name': user
                }
            })
            # update single use token for group
            group_token = create_group_token(group_ref.key)
            group_ref.update({
                'token': group_token
            })
            # Update users current group
            user_ref = db.reference("/users/" + uid)
            user_ref.update({ "current_group": group_ref.key })
            #Notify users in group
            message.send()
            return make_response('added to group', 200)
        else:
            return make_response('incorrect token or already member of group', 401)
    except Exception as ex:
        return make_response('Joining group unsuccessful', 500)


#API endpoint for deleting groups
# Looks for the following headers in the request:
# Authorization: Verification token which the client can request from Firebase
# group_id: Id of the group which the user wants to leave/delete
@app.route("/api/deletegroup", methods=["DELETE"])
def delete_group():
    #get headers
    data = request.headers
    id_token = data['Authorization']
    try:
        decoded_token = auth.verify_id_token(id_token)
    except Exception as ex:
        return make_response('user not found', 401)
    # get user information
    uid = decoded_token["uid"]
    group_id = data["group_id"]
    user = decoded_token["name"]
    try:
        # get reference to group by id
        group_ref = db.reference("/groups/" + group_id)
        group_dict = group_ref.get()
        members_list = group_dict["members"].keys()
        #uid belongs to owner -> Delete group
        if uid ==  group_dict['owner']['id']:
            #Prepare message
            message_title = "Group deleted!"
            message_body = "The group " + group_dict['groupname'] + " has been deleted by the owner"
            message = Message(group_id, message_title, message_body)
            #Create resource manager
            rm = ResourceManager(group_id)
            #Set the current group of everyone who was in this group to null
            for member in members_list:
                user_group_ref = db.reference("/users/" + member + "/current_group")
                user_group_ref.delete()
            #Clean all resources
            group_ref.delete()
            rm.delete_blobs()
            # Notify all users in group
            message.send()
            return make_response('group deleted', 200)
        #uid belongs to member -> Leave group
        elif uid in group_dict['members']:
            #Set the current group of user to null
            group_ref.child('members/' + uid).delete()
            user_group_ref = db.reference("/users/" + uid + "/current_group")
            user_group_ref.delete()
            # Notify all users in group
            message_title = "A member left your group!"
            message_body = user + " has left your group " + group_dict['groupname']
            message = Message(group_id, message_title, message_body)
            message.send()
            return make_response('left group', 200)
        else:
            return make_response('not authorized', 401)
    except Exception as ex:
        return make_response('Leaving group unsuccessful', 500)


#helper method for creating the 128-bit custom token
def create_group_token(group_id):
    token = group_id+" "+secrets.token_hex(32)
    return token

def parse_group_id(token):
   group_id, otp = token.split(' ')
   return group_id


if __name__ == "__main__":
    app.run(host='127.0.0.1', port=8080, debug=True)
