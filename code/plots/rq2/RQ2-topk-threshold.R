#topk_threshold
filecols = c("Prec@5","Prec@10","MRR","MAP","technique","functionality","time","seed")

ptkT1_topk_threshold<-read.csv("~/treekernel-emse2025/results/RQ2/RQ2_T1_PTK/metrics_RQ2_T1_PTK_topk_threshold.csv",header = FALSE)
colnames(ptkT1_topk_threshold)<-filecols
ptkT1_topk_threshold$type<-"T1"
ptkT1_topk_threshold$filter<-"topk_threshold"
ptkT1_topk_threshold$kernel<-"PTK"

ptkT2_topk_threshold<-read.csv("~/treekernel-emse2025/results/RQ2/RQ2_T2_PTK/metrics_RQ2_PTK_T2_topk_threshold.csv",header = FALSE)
colnames(ptkT2_topk_threshold)<-filecols
ptkT2_topk_threshold$type<-"T2"
ptkT2_topk_threshold$filter<-"topk_threshold"
ptkT2_topk_threshold$kernel<-"PTK"

ptkT3_topk_threshold<-read.csv("~/treekernel-emse2025/results/RQ2/RQ2_T3_PTK/metrics_RQ2_PTK_topk_threshold.csv",header = FALSE)
colnames(ptkT3_topk_threshold)<-filecols
ptkT3_topk_threshold$type<-"T3"
ptkT3_topk_threshold$filter<-"topk_threshold"
ptkT3_topk_threshold$kernel<-"PTK"

sstkT1_topk_threshold<-read.csv("~/treekernel-emse2025/results/RQ2/RQ2_T1_SSTK/metrics_RQ2_T1_SSTK_topk_threshold.csv",header = FALSE)
colnames(sstkT1_topk_threshold)<-filecols
sstkT1_topk_threshold$type<-"T1"
sstkT1_topk_threshold$filter<-"topk_threshold"
sstkT1_topk_threshold$kernel<-"SSTK"

sstkT2_topk_threshold<-read.csv("~/treekernel-emse2025/results/RQ2/RQ2_T2_SSTK/metrics_RQ2_T2_SSTK_topk_threshold.csv",header = FALSE)
colnames(sstkT2_topk_threshold)<-filecols
sstkT2_topk_threshold$type<-"T2"
sstkT2_topk_threshold$filter<-"topk_threshold"
sstkT2_topk_threshold$kernel<-"SSTK"

sstkT3_topk_threshold<-read.csv("~/treekernel-emse2025/results/RQ2/RQ2_T3_SSTK/metrics_RQ2_T3_SSTK_topk_threshold.csv",header = FALSE)
colnames(sstkT3_topk_threshold)<-filecols
sstkT3_topk_threshold$type<-"T3"
sstkT3_topk_threshold$filter<-"topk_threshold"
sstkT3_topk_threshold$kernel<-"SSTK"

stkT1_topk_threshold<-read.csv("~/treekernel-emse2025/results/RQ2/RQ2_T1_STK/metrics_RQ2_T1_STK_topk_threshold.csv",header = FALSE)
colnames(stkT1_topk_threshold)<-filecols
stkT1_topk_threshold$type<-"T1"
stkT1_topk_threshold$filter<-"topk_threshold"
stkT1_topk_threshold$kernel<-"STK"

stkT2_topk_threshold<-read.csv("~/treekernel-emse2025/results/RQ2/RQ2_T2_STK/metrics_rq2_T2_STK_topk_threshold.csv",header = FALSE)
colnames(stkT2_topk_threshold)<-filecols
stkT2_topk_threshold$type<-"T2"
stkT2_topk_threshold$filter<-"topk_threshold"
stkT2_topk_threshold$kernel<-"STK"

stkT3_topk_threshold<-read.csv("~/treekernel-emse2025/results/RQ2/RQ2_T3_STK/metrics_RQ2_T3_STK_topk_threshold.csv",header = FALSE)
colnames(stkT3_topk_threshold)<-filecols
stkT3_topk_threshold$type<-"T3"
stkT3_topk_threshold$filter<-"topk_threshold"
stkT3_topk_threshold$kernel<-"STK"

