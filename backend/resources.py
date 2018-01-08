from firebase_admin import db
from google.cloud import storage
import re

class ResourceManager():

    def __init__(self, group_id):
        self.client = storage.Client()
        self.bucket_id = "mcc-fall-2017-g03.appspot.com"
        self.bucket = self.client.get_bucket(self.bucket_id)
        self.group_id = group_id
        self.blob_list = self.get_blob_name_list()

    def get_blob_name_list(self):
        blob_name_list = []
        group_images_ref = db.reference("/groups/" + self.group_id + "/images")
        group_images_dict = group_images_ref.get()
        image_list_map = list(group_images_dict.values())
        for image_data in image_list_map:
            url_full = image_data['url'].split('.com/')[1]
            url_base = re.compile("-full.jpg$|-high.jpg$|-low.jpg$").split(url_full)[0]
            blob_name_list.append(url_base + '-full.jpg')
            blob_name_list.append(url_base + '-high.jpg')
            blob_name_list.append(url_base + '-low.jpg')
        return blob_name_list

    def delete_blobs(self):
        self.bucket.delete_blobs(self.blob_list)