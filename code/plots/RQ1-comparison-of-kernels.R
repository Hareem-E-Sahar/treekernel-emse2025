library(dplyr)
library(ggplot2)   
library(tidyr)
mycolname<-c("Prec@5","Prec@10","MRR","MAP","technique","functionality","time","seed")
ptk<-read.csv("~/treekernel-emse2025/results/RQ1/PTK/metrics_rq1_PTK_topk.csv", header=FALSE)
colnames(ptk)<-mycolname
ptk$kernel<-"PTK"
stk<-read.csv("~/treekernel-emse2025/results/RQ1/STK/metrics_rq1_STK_topk.csv", header=FALSE)
colnames(stk)<-mycolname
stk$kernel<-"STK"
sstk<-read.csv("~/treekernel-emse2025/results/RQ1/SSTK/metrics_rq1_SSTK_topk.csv", header=FALSE)
colnames(sstk)<-mycolname
sstk$kernel<-"SSTK"
esdf<-read.csv("~/treekernel-emse2025/results/RQ1/tfidf/metrics_elasticsearch.csv")
colnames(esdf)<-mycolname
esdf$kernel<-"TF-IDF"

combined_data <- bind_rows(stk, sstk, ptk, esdf)
long_data <- combined_data %>%
  pivot_longer(cols = c(`Prec@5`,`Prec@10`, MRR, MAP), 
               names_to = "Metric", 
               values_to = "Value")

all_plots<-ggplot(long_data, aes(x = kernel, y = Value, fill = kernel)) +
  geom_boxplot() +
  facet_wrap(~ Metric, scales = "free_y") +  # Facet by Metric, adjust y-axis independently for each
  labs( y = "",x = "") +
  scale_fill_manual(values = c("PTK" = "lightblue",  "SSTK"="lightgreen", "STK" = "grey")) +
  theme_minimal() +
  theme(legend.title = element_blank(),
        axis.text.x = element_blank())
ggsave("~/treekernel-emse2025/results/RQ1/rq1-topk-kernels.pdf",all_plots,width=5,height=4)


all_plots<-ggplot(long_data, aes(x = kernel, y = Value, fill = kernel)) +
  geom_boxplot() +
  facet_wrap(~ Metric, scales = "free_y") +  # Facet by Metric, adjust y-axis independently for each
  labs( y = "",x = "") +
  scale_fill_manual(values = c("PTK" = "lightblue",  "SSTK"="lightgreen", "STK" = "grey","TF-IDF"="pink")) +
  theme_minimal() +
  theme(legend.title = element_blank(),
        axis.text.x = element_blank())
ggsave("~/treekernel-emse2025/results/RQ1/rq1-topk-comparison-of-kernels-and-tfidf.pdf",all_plots,width=5,height=4)

# p-value <= 0.05/12 = 0.004166667, the result is stat sig, so you reject the null hypothesis

#PTK & STK
wilcox.test(ptk$Prec.5, stk$Prec.5)   #p-value = 0.003886, reject
wilcox.test(ptk$Prec.10, stk$Prec.10) #p-value = 0.00105,  reject
wilcox.test(ptk$MRR, stk$MRR)         #p-value = 0.07526,  null hypothesis is true
wilcox.test(ptk$MAP, stk$MAP)         #p-value = 0.002879, reject
#PTK & SSTK
wilcox.test(ptk$Prec.5, sstk$Prec.5)  #p-value = 1.083e-05, reject null
wilcox.test(ptk$Prec.10, sstk$Prec.10)#p-value = 1.083e-05, reject null
wilcox.test(ptk$MRR, sstk$MRR)        #p-value = 0.5787,    null hypothesis is true, no diff
wilcox.test(ptk$MAP, sstk$MAP)        #p-value = 0.03546    null hypothesis is true, no diff
#STK & SSTK
wilcox.test(stk$Prec.5, sstk$Prec.5) #p-value = 1.083e-05, reject
wilcox.test(stk$Prec.10, sstk$Prec.10)#p-value = 1.083e-05, reject
wilcox.test(stk$MRR, sstk$MRR) #p-value = 0.5288, null hypothesis is true
wilcox.test(stk$MAP, sstk$MAP) #p-value = 1.083e-05, reject

#TF-IDF vs SSTK
wilcox.test(esdf$Prec.5,sstk$Prec.5)   #p-value = 2.891e-06 reject null. There is diff
wilcox.test(esdf$Prec.10,sstk$Prec.10) #p-value = 2.891e-06 reject null. There is diff
wilcox.test(esdf$MRR,sstk$MRR)         #p-value = 2.891e-06 reject null. There is diff
wilcox.test(esdf$MAP,sstk$MAP)         #p-value = 0.0005443 reject null. There is diff

#TF-IDF vs STK
wilcox.test(esdf$Prec.5,stk$Prec.5)    #p-value = 0.008975, reject null
wilcox.test(esdf$Prec.10,stk$Prec.10)  #p-value = 0.005098, reject null
wilcox.test(esdf$MRR,stk$MRR)          #p-value = 2.891e-06, reject null
wilcox.test(esdf$MAP,stk$MAP)          #p-value = 2.891e-06, reject null

#TF-IDF vs PTK
wilcox.test(esdf$Prec.5,ptk$Prec.5)    #p-value = 0.03206, cannot reject null
wilcox.test(esdf$Prec.10,ptk$Prec.10)  #p-value = 7.102e-06, reject null
wilcox.test(esdf$MRR,ptk$MRR)          #p-value = 2.891e-06, reject null
wilcox.test(esdf$MAP,ptk$MAP)          #p-value = 2.891e-06, reject null

#Table 1
#PTK
summary(ptk$Prec.5)
summary(ptk$Prec.10)
summary(ptk$MRR)
summary(ptk$MAP)

#STK and SSTK as above
#elasticsearch
summary(esdf$Prec.5)
summary(esdf$Prec.10)
summary(esdf$MRR)
summary(esdf$MAP)
summary(esdf$time)

#Pairwise comparison per kernel by stacking metrics where alpha = 0.05/6 = 0.0083
stk_all <-c(stk$Prec.5, stk$Prec.10, stk$MRR, stk$MAP)
ptk_all <-c(ptk$Prec.5, ptk$Prec.10, ptk$MRR, ptk$MAP)
sstk_all<-c(sstk$Prec.5, sstk$Prec.10, sstk$MRR, sstk$MAP)
esdf_all<-c(esdf$Prec.5, esdf$Prec.10, esdf$MRR, esdf$MAP)

# P-value < alpha is deemed to be statistically significant, meaning the null hypothesis should be rejected in such a case.
#STK & SSTK pairwise comparison
wilcox.test(stk_all, sstk_all, paired=TRUE) #p-value = 6.748e-10, reject null
#PTK & STK pairwise comparison 
wilcox.test(stk_all, ptk_all, paired=TRUE)  #p-value = 1.827e-07, reject null
#PTK & SSTK pairwise comparison
wilcox.test(ptk_all, sstk_all, paired=TRUE) #p-value = 1.95e-05, reject null

#TF-IDF vs SSTK pairwise comparison
wilcox.test(esdf_all, sstk_all, paired=TRUE) #p-value = 3.49e-07, reject null
#TF-IDF vs STK pairwise comparison
wilcox.test(esdf_all, stk_all, paired=TRUE)  #p-value  = 0.01411, cannot reject null
#TF-IDF vs PTK pairwise comparison
wilcox.test(esdf_all, ptk_all, paired=TRUE)  #p-value  = 1.544e-06, reject null

