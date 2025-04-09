#topk
filecols = c("Prec@5","Prec@10","MRR","MAP","technique","functionality","time","seed")
ptkT1<-read.csv("~/treekernel-emse2025/results/RQ2/RQ2_T1_PTK/metrics_RQ2_T1_PTK_topk.csv",header = FALSE)
colnames(ptkT1)<-filecols
ptkT1$type<-"T1"
ptkT1$filter<-"topk"
ptkT1$kernel<-"PTK"

ptkT2<-read.csv("~/treekernel-emse2025/results/RQ2/RQ2_T2_PTK/metrics_RQ2_PTK_T2_topk.csv",header = FALSE)
colnames(ptkT2)<-filecols
ptkT2$type<-"T2"
ptkT2$filter<-"topk"
ptkT2$kernel<-"PTK"

ptkT3<-read.csv("~/treekernel-emse2025/results/RQ2/RQ2_T3_PTK/metrics_RQ2_PTK_topk.csv",header = FALSE)
colnames(ptkT3)<-filecols
ptkT3$type<-"T3"
ptkT3$filter<-"topk"
ptkT3$kernel<-"PTK"

sstkT1<-read.csv("~/treekernel-emse2025/results/RQ2/RQ2_T1_SSTK/metrics_RQ2_T1_SSTK_topk.csv",header = FALSE)
colnames(sstkT1)<-filecols
sstkT1$type<-"T1"
sstkT1$filter<-"topk"
sstkT1$kernel<-"SSTK"

sstkT2<-read.csv("~/treekernel-emse2025/results/RQ2/RQ2_T2_SSTK/metrics_RQ2_T2_SSTK_topk.csv",header = FALSE)
colnames(sstkT2)<-filecols
sstkT2$type<-"T2"
sstkT2$filter<-"topk"
sstkT2$kernel<-"SSTK"

sstkT3<-read.csv("~/treekernel-emse2025/results/RQ2/RQ2_T3_SSTK/metrics_RQ2_T3_SSTK_topk.csv",header = FALSE)
colnames(sstkT3)<-filecols
sstkT3$type<-"T3"
sstkT3$filter<-"topk"
sstkT3$kernel<-"SSTK"

stkT1<-read.csv("~/treekernel-emse2025/results/RQ2/RQ2_T1_STK/metrics_RQ2_T1_STK_topk.csv",header = FALSE)
colnames(stkT1)<-filecols
stkT1$type<-"T1"
stkT1$filter<-"topk"
stkT1$kernel<-"STK"

stkT2<-read.csv("~/treekernel-emse2025/results/RQ2/RQ2_T2_STK/metrics_RQ2_T2_STK_topk.csv",header = FALSE)
colnames(stkT2)<-filecols
stkT2$type<-"T2"
stkT2$filter<-"topk"
stkT2$kernel<-"STK"

stkT3<-read.csv("~/treekernel-emse2025/results/RQ2/RQ2_T3_STK/metrics_RQ2_T3_STK_topk.csv",header = FALSE)
colnames(stkT3)<-filecols
stkT3$type<-"T3"
stkT3$filter<-"topk"
stkT3$kernel<-"STK"

