library(dplyr)
library(ggplot2)
library(tidyr)

mycolname<-c("Prec@5","Prec@10","MRR","MAP","technique","functionality","time","seed")
ptk<-read.csv("~/treekernel-emse2025/results/RQ1/PTK/metrics_rq1_PTK_topk_threshold.csv",header=FALSE)
colnames(ptk)<-mycolname
ptk$kernel<-"PTK"
ptk$group<-"PTK"
stk<-read.csv("~/treekernel-emse2025/results/RQ1/STK/metrics_rq1_STK_topk_threshold.csv",header=FALSE)
colnames(stk)<-mycolname
stk$kernel<-"STK"
stk$group<-"STK"
sstk<-read.csv("~/treekernel-emse2025/results/RQ1/SSTK/metrics_rq1_SSTK_topk_threshold.csv",header=FALSE)
colnames(sstk)<-mycolname
sstk$kernel<-"SSTK"
sstk$group<-"SSTK"

# TF-IDF
esdf<-read.csv("~/treekernel-emse2025/results/RQ1/tfidf/metrics_elasticsearch.csv") #new
colnames(esdf)<-mycolname
esdf$kernel<-"TF-IDF"
esdf$group<-"TF-IDF"

# Hybrid
hybrid_sstk <- read.csv("~/treekernel-emse2025/results/RQ4/SSTK/metrics_rq4_hybrid_SSTK_topk_threshold.csv",header=FALSE)
colnames(hybrid_sstk)<-mycolname
hybrid_sstk$kernel<-"Hybrid-SSTK"
hybrid_sstk$group<-"SSTK"


hybrid_stk <- read.csv("~/treekernel-emse2025/results/RQ4/STK/metrics_rq4_hybrid_STK_topk_threshold.csv",header=FALSE)
colnames(hybrid_stk)<-mycolname
hybrid_stk$kernel<-"Hybrid-STK"
hybrid_stk$group<-"STK"

hybrid_ptk <- read.csv("~/treekernel-emse2025/results/RQ4/PTK/metrics_rq4_hybrid_PTK_topk_threshold.csv",header = FALSE)
colnames(hybrid_ptk)<-mycolname
hybrid_ptk$kernel<-"Hybrid-PTK"
hybrid_ptk$group<-"PTK"

combined_data_rq4 <- bind_rows(stk, sstk, ptk, hybrid_stk, hybrid_sstk, hybrid_ptk, esdf)

long_data_rq4 <- combined_data_rq4 %>%
  pivot_longer(cols = c(`Prec@5`, `Prec@10`, MRR, MAP), 
               names_to = "Metric", 
               values_to = "Value")

long_data_rq4 <- long_data_rq4 %>%
  mutate(GroupKernel = paste(group, kernel))

rq4plot_colored <- ggplot(long_data_rq4, aes(x = factor(paste(group, kernel),
                          level = c("STK STK", "SSTK SSTK", "PTK PTK",
                          "STK Hybrid-STK", "SSTK Hybrid-SSTK", "PTK Hybrid-PTK", "TF-IDF TF-IDF")), 
                          y = Value, fill = factor(paste(group, kernel)))) +
                          geom_boxplot() + # Removed `fill = "white"` to enable custom colors
                          facet_wrap(~ Metric, scales = "free_y") + 
                          theme_minimal() +
                          theme(
                            legend.position = 'none',
                            axis.text.x = element_text(angle = 45, hjust = 1),
                            axis.title = element_blank()
                          ) + 
                        scale_x_discrete(
                          labels = c(
                            "STK STK" = "STK",
                            "SSTK SSTK" = "SSTK",
                            "PTK PTK" = "PTK",
                            "STK Hybrid-STK" = "Hybrid-STK",
                            "SSTK Hybrid-SSTK" = "Hybrid-SSTK",
                            "PTK Hybrid-PTK" = "Hybrid-PTK",
                            "TF-IDF TF-IDF" = "TF-IDF"
                          ) ) +
                        scale_fill_manual(
                          values = c(
                            "STK STK" = "#aec7e8",      # Blue for STK
                            "SSTK SSTK" = "#ff9896",    # pink for SSTK
                            "PTK PTK" = "#98df8a",      # Green for PTK
                            "STK Hybrid-STK" = "#aec7e8",  
                            "SSTK Hybrid-SSTK" = "#ff9896", 
                            "PTK Hybrid-PTK" = "#98df8a",  
                            "TF-IDF TF-IDF" = "orange"    # orange for TF-IDF
                          ))

ggsave("~/treekernel-emse2025/results/RQ4/RQ4-topk-threshold-colored.pdf",rq4plot_colored,width=6,height=4)

#######################################################################################################################

stk_all <-c(stk$Prec.5, stk$Prec.10, stk$MRR, stk$MAP)
ptk_all <-c(ptk$Prec.5, ptk$Prec.10, ptk$MRR, ptk$MAP)
sstk_all<-c(sstk$Prec.5, sstk$Prec.10, sstk$MRR, sstk$MAP)
esdf_all<-c(esdf$Prec.5, esdf$Prec.10, esdf$MRR, esdf$MAP)


hybrid_stk_all <-c(hybrid_stk$Prec.5, hybrid_stk$Prec.10, hybrid_stk$MRR, hybrid_stk$MAP)
hybrid_ptk_all <-c(hybrid_ptk$Prec.5, hybrid_ptk$Prec.10, hybrid_ptk$MRR, hybrid_ptk$MAP)
hybrid_sstk_all<-c(hybrid_sstk$Prec.5, hybrid_sstk$Prec.10, hybrid_sstk$MRR, hybrid_sstk$MAP)

# P-value < alpha is deemed to be statistically significant, meaning the null hypothesis should be rejected in such a case.
#STK & SSTK pairwise comparison
wilcox.test(stk_all, hybrid_stk_all, paired=TRUE) #p-value =  1.907e-06, reject null
wilcox.test(ptk_all, hybrid_ptk_all, paired=TRUE) #p-value =  1.907e-06, reject null
wilcox.test(sstk_all, hybrid_sstk_all, paired=TRUE) #p-value = 0.0001678, reject null

wilcox.test(esdf_all, hybrid_stk_all, paired=TRUE) #p-value =  0.9273, cannot reject null
wilcox.test(esdf_all, hybrid_ptk_all, paired=TRUE) #p-value =  0.7841, cannot reject null
wilcox.test(esdf_all, hybrid_sstk_all, paired=TRUE) #p-value = 0.2611, cannot reject null

########################################################################################################################

#RQ1 results
#TOP-K FILTER
PTK   & 0.64  & 0.60 & 0.81 & 0.07 #Best Prec but MAP is same as TOPK_THRESHOLD  
STK   & 0.68  & 0.64 & 0.83 & 0.08
SSTK  & 0.60  & 0.56 & 0.77 & 0.07
TFIDF & 0.73  & 0.70 & 0.91 & 0.09 #"~/treekernel-emse2025/results/RQ1/tfidf/metrics_elasticsearch.csv"

#THRESHOLD
PTK   & 0.56 & 0.51 & 0.79 & 0.16 #higher MAP than TOPK and THRESHOLD
STK   & 0.60 & 0.57 & 0.82 & 0.19
SSTK  & 0.40 & 0.38 & 0.76 & 0.11
TFIDF & 0.73  & 0.70 & 0.91 & 0.09 #"~/treekernel-emse2025/results/RQ1/tfidf/metrics_elasticsearch.csv"

#TOPK_THRESHOLD
PTK   & 0.56 & 0.51 & 0.80  & 0.06 #lower precision than topk, higher MRR than threhsold 
STK   & 0.60 & 0.57 & 0.83 & 0.07
SSTK  & 0.40 & 0.38 & 0.81 & 0.06
TFIDF & 0.73  & 0.70 & 0.91 & 0.09 #"~/treekernel-emse2025/results/RQ1/tfidf/metrics_elasticsearch.csv"

########################################################################################################################

#  combined_data_rq4$Group <- ifelse(
#  combined_data_rq4$kernel %in% c("ptk", "stk", "sstk"),
#  substr(combined_data_rq4$kernel, 1, 3),
#  substr(combined_data_rq4$kernel, 7, 10))

# Condition (combined_data_rq4$kernel %in% c("ptk", "stk", "sstk")):
# This part checks whether the kernel value is one of "ptk", "stk", or "sstk".
# If the condition is TRUE for a row, the code will execute the "TRUE" portion of the ifelse.
# TRUE case (substr(combined_data_rq4$kernel, 1, 3)):
#   
# If the kernel value is "ptk", "stk", or "sstk", the Group column is assigned the first three characters of the kernel value using substr(combined_data_rq4$kernel, 1, 3).
# For instance, if kernel is "ptk", then Group will be "ptk".
# FALSE case (substr(combined_data_rq4$kernel, 7, 10)):
#   
# If the kernel value is not "ptk", "stk", or "sstk", the Group column is assigned characters from positions 7 to 10 of kernel.
# This is intended for cases where kernel is a hybrid name like "Hybrid-PTK" or "Hybrid-STK".
# For instance, if kernel is "Hybrid-PTK", then Group will be "PTK".
