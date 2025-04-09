#Threshold
filecols = c("Prec@5","Prec@10","MRR","MAP","technique","functionality","time","seed")

ptkT1_threshold<-read.csv("~/treekernel-emse2025/results/RQ2/RQ2_T1_PTK/metrics_RQ2_T1_PTK_threshold.csv",header = FALSE)
colnames(ptkT1_threshold)<-filecols
ptkT1_threshold$type<-"T1"
ptkT1_threshold$filter<-"threshold"
ptkT1_threshold$kernel<-"PTK"

ptkT2_threshold<-read.csv("~/treekernel-emse2025/results/RQ2/RQ2_T2_PTK/metrics_RQ2_PTK_T2_threshold.csv",header = FALSE)
colnames(ptkT2_threshold)<-filecols
ptkT2_threshold$type<-"T2"
ptkT2_threshold$filter<-"threshold"
ptkT2_threshold$kernel<-"PTK"

ptkT3_threshold<-read.csv("~/treekernel-emse2025/results/RQ2/RQ2_T3_PTK/metrics_RQ2_PTK_threshold.csv",header = FALSE)
colnames(ptkT3_threshold)<-filecols
ptkT3_threshold$type<-"T3"
ptkT3_threshold$filter<-"threshold"
ptkT3_threshold$kernel<-"PTK"

sstkT1_threshold<-read.csv("~/treekernel-emse2025/results/RQ2/RQ2_T1_SSTK/metrics_RQ2_T1_SSTK_threshold.csv",header = FALSE)
colnames(sstkT1_threshold)<-filecols
sstkT1_threshold$type<-"T1"
sstkT1_threshold$filter<-"threshold"
sstkT1_threshold$kernel<-"SSTK"

sstkT2_threshold<-read.csv("~/treekernel-emse2025/results/RQ2/RQ2_T2_SSTK/metrics_RQ2_T2_SSTK_threshold.csv",header = FALSE)
colnames(sstkT2_threshold)<-filecols
sstkT2_threshold$type<-"T2"
sstkT2_threshold$filter<-"threshold"
sstkT2_threshold$kernel<-"SSTK"

sstkT3_threshold<-read.csv("~/treekernel-emse2025/results/RQ2/RQ2_T3_SSTK/metrics_RQ2_T3_SSTK_threshold.csv",header = FALSE)
colnames(sstkT3_threshold)<-filecols
sstkT3_threshold$type<-"T3"
sstkT3_threshold$filter<-"threshold"
sstkT3_threshold$kernel<-"SSTK"

stkT1_threshold<-read.csv("~/treekernel-emse2025/results/RQ2/RQ2_T1_STK/metrics_RQ2_T1_STK_threshold.csv",header = FALSE)
colnames(stkT1_threshold)<-filecols
stkT1_threshold$type<-"T1"
stkT1_threshold$filter<-"threshold"
stkT1_threshold$kernel<-"STK"

stkT2_threshold<-read.csv("~/treekernel-emse2025/results/RQ2/RQ2_T2_STK/metrics_RQ2_T2_STK_threshold.csv",header = FALSE)
colnames(stkT2_threshold)<-filecols
stkT2_threshold$type<-"T2"
stkT2_threshold$filter<-"threshold"
stkT2_threshold$kernel<-"STK"

stkT3_threshold<-read.csv("~/treekernel-emse2025/results/RQ2/RQ2_T3_STK/metrics_RQ2_T3_STK_threshold.csv",header = FALSE)
colnames(stkT3_threshold)<-filecols
stkT3_threshold$type<-"T3"
stkT3_threshold$filter<-"threshold"
stkT3_threshold$kernel<-"STK"
