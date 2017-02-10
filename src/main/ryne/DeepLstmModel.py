#coding=UTF-8
'''
Created on 2016年8月10日
anti cheat using lstm model
@author: wangshuai
'''

from keras.models import model_from_json
import numpy
from keras.preprocessing import sequence
from theano.tensor.shared_randomstreams import RandomStreams
import sys
import time

def fromFile2Index(filePath):
    outDict = {}
    idx = 0
    with open(filePath) as dicFile:
        lines = dicFile.readlines()
        for line in lines:
            temp = line.strip().split("\t")
            word = temp[0]
            outDict[word]=idx
            idx +=1
    return outDict


def fromFile2Data(filePath, wordDic={}):
    outLines = []
    with open(filePath, 'r') as inputFile:
        #filter words length >=5
        lines = [element.split("\t") for element in inputFile.readlines() if (len(element.split("\t")[1].split(","))>=5)]

        did = [ element[0] for element in lines]

        data = map(lambda x: [ wordDic[ele] if wordDic.has_key(ele) else wordDic["UNKNOWN"]  for ele in x[1].split(",") ], lines)

        outdata = [ element for element in data if float(element.count("UNKNOWN"))/len(element)<=0.4 ]
    return did, outdata


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


def lstm_predict(lstm_model, word_dic, to_predict_file, predict_res_file, gpuMode = False):
    with open(predict_res_file, 'w') as res:
        did, data = fromFile2Data(to_predict_file, word_dic)
        print "local predict data done"
        print data[0:100]
        if gpuMode:
            data_predict_seq = sequence.pad_sequences(data, maxlen=500)
            label = lstm_model.predict(data_predict_seq, batch_size=1024)
            out = zip(did, [ele[0] for ele in label])
            for element in out:
                res.writelines( "{0},{1}\n".format(element[0],  str(element[1])))

        else:
            data_cnt = len(did)
            idx = 0
            part_cnt = data_cnt/1024
            print "data part cnt is, ", part_cnt

            while (idx+1) * 1024 <= data_cnt:
                time_start = time.time()
                sub_did = did[idx*1024: (idx+1) * 1024]
                sub_data = data[idx*1024: (idx+1) * 1024]

                data_predict_seq = sequence.pad_sequences(sub_data, maxlen=500)
                label = lstm_model.predict(data_predict_seq, batch_size=256)
                out = zip(sub_did, [ele[0] for ele in label])
                for element in out:
                    res.writelines( "{0}\t{2}\t{1}\n".format(element[0], str(element[1]), "1" if (element[1]>0.5) else "0"))

                time_end = time.time()
                time_cost = time_end - time_start

                idx += 1
                print "iter: ",idx
                print "current idx time cost is {0} seconds, estimate remaining time is {1} seconds".format(time_cost, time_cost * (part_cnt - idx) )


            sub_did = did[idx * 1024 : -1]
            sub_data = data[idx * 1024 : -1]

            data_predict_seq = sequence.pad_sequences(sub_data, maxlen=500)
            label = lstm_model.predict(data_predict_seq, batch_size=256)
            out = zip(sub_did, [ele[0] for ele in label])
            for element in out:
                res.writelines( "{0},{1}\n".format(element[0],  str(element[1]) ) )

if __name__ == '__main__':
    if ( len(sys.argv) < 6 ) :
        print "not enough arguments"
    else:
        numpy.random.seed(7)
        srng = RandomStreams(7)

        model_json_file = sys.argv[1]
        model_weight_h5 = sys.argv[2]

        to_predict_file = sys.argv[3]
        predict_res_file = sys.argv[4]

        dict_file = sys.argv[5]

        word_dict = fromFile2Index(dict_file)
        print "load dict done"

        lstm_model = load_model(model_json_file, model_weight_h5)
        print "load model done"

        lstm_predict(lstm_model, word_dict, to_predict_file, predict_res_file, True)
        print "predict done"
    pass