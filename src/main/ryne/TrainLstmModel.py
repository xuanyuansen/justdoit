#coding=UTF-8
'''
Created on 2016年8月17日
train lstm model
@author: wangshuai
'''
import numpy as np
from keras.models import Sequential
from keras.layers import Dense, Activation
from keras.layers.embeddings import Embedding
from keras.layers import Dense
from keras.layers import LSTM
import random
import sys
from theano.tensor.shared_randomstreams import RandomStreams
from keras.preprocessing import sequence

def fromFile2Index(filePath, dimension=128):
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

def fromFile2SimpleData(filePath, wordDic={}):
    with open(filePath, 'r') as inputFile:
        #filter 1 to get correct data
        lines = [element.split("\t") for element in inputFile.readlines() if (len(element.split("\t"))==2)]
        random.shuffle(lines)
        #filter 2 to get length >=5
        filterLines = [element for element in lines if ( len( element[1].split(",") )>=5 )]
        filterLines = [[element[0],element[1][0:1000]] if ( len( element[1].split(",") )>=1000 ) else element for element in lines ]
        outLines = [ (1.0 if element[0]=="1" else 0.0, element[1].split(",")) for element in filterLines]
        label = map(lambda x: 1 if x[0]==1.0 else 0,outLines)
        data = map(lambda x: [ wordDic[ele] if wordDic.has_key(ele) else wordDic["UNKNOWN"]  for ele in x[1] ],outLines)
    return label,data

if __name__ == '__main__':
    print "usage"
    print "arg1: dict arg2: train_file arg3: train_data_num"
    np.random.seed(7)
    srng = RandomStreams(7)

    # create the model
    model = Sequential()
    model.add(Embedding(1000, 64))
    model.add(LSTM(64,dropout_W=0.5,dropout_U=0.5))
    model.add(Dense(1, activation='sigmoid'))
    model.compile(loss='binary_crossentropy', optimizer='adam', metrics=['accuracy'])
    print(model.summary())

    index_dict = fromFile2Index(sys.argv[1])
    label, train_data =fromFile2SimpleData(sys.argv[2], index_dict)
    print len(label), len(train_data)


    train_x = train_data[0:int(sys.argv[3])]
    train_y = label[0:int(sys.argv[3])]

    test_x = train_data[int(sys.argv[3]):-1]
    test_y = label[int(sys.argv[3]):-1]

    X_train = sequence.pad_sequences(train_x, maxlen=500)
    X_test = sequence.pad_sequences(test_x, maxlen=500)

    model.fit(X_train, train_y, nb_epoch=20, batch_size=64)

    scores_train = model.evaluate(X_train, train_y, batch_size=1024,verbose=1)
    print("Train Accuracy: %.2f%%" % (scores_train[1]*100))

    scores_test = model.evaluate(X_test, test_y, batch_size=1024, verbose=1)
    print("Test Accuracy: %.2f%%" % (scores_test[1]*100))

    model_json = model.to_json()
    with open("model_keras_lstm.json", "w") as json_file:
        json_file.write(model_json)

    # serialize weights to HDF5
    model.save_weights("model_keras_lstm.h5")
    print("Saved model to disk done, finished!")

    pass