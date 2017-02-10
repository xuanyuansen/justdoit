#!/bin/sh
THEANO_FLAGS='floatX=float32,device=gpu2,lib.cnmem=0,optimizer_including=cudnn' python ./Sequential-Model-Util/src/main/python/TrainLstmModel.py ./Sequential-Model-Util/daily_data/kerasmodel/event_dict ./Sequential-Model-Util/daily_data/kerasmodel/event_train_0801_0907 2000000 >& info.log
