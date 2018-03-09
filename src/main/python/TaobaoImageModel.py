# -*- coding: utf-8 -*-
import numpy as np
import tensorflow as tf
from keras.models import model_from_json
import os
import RawImageHelper
import gc


def load_model(model_json_file, model_weight_h5):
    # load json and create model
    json_file = open(model_json_file, 'r')
    loaded_model_json = json_file.read()
    json_file.close()
    loaded_model = model_from_json(loaded_model_json)
    # load weights into new model
    loaded_model.load_weights(model_weight_h5)
    print("Loaded model from h5")
    return loaded_model

if __name__ == "__main__":
    np.random.seed(8)
    tf.set_random_seed(8)
    resnet_model = load_model("model.json", "model.weights")
    source_dir = "/home/wangliaofan/Documents/fandan"
    for parent, dir_names, file_names in os.walk(source_dir):
        for file_name in file_names:
            target = "{0}/{1}".format(parent, file_name)
            img_src = np.array(RawImageHelper.read_raw_image(target))
            data = np.zeros((1, 32, 32, 3), dtype='uint8')
            data[0, :, :, :] = img_src
            res = resnet_model.predict(data)
            print(max(res[0]))
            # print(max(res[0]))
            print(target)
            print(res[0].argmax())
            print("====================================")
    gc.collect()
    pass
