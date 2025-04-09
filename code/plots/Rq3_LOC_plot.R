#RQ3 using threshold and topdocs filter criteria
library(dplyr)
library(ggplot2)   
library(tidyr)

ptk_lessThan6LOC<-read.csv("~/treekernel-emse2025/results/RQ3-Size/lessThan6LOC/topk_threshold/metrics_RQ3_PTK.csv") 
sstk_lessThan6LOC<-read.csv("~/treekernel-emse2025/results/RQ3-Size/lessThan6LOC/topk_threshold/metrics_RQ3_SSTK.csv")
stk_lessThan6LOC<-read.csv("~/treekernel-emse2025/results/RQ3-Size/lessThan6LOC/topk_threshold/metrics_RQ3_STK.csv")

ptk_moreThan10LOC<-read.csv("~/treekernel-emse2025/results/RQ3-Size/moreThan10LOC/topk_threshold/metrics_RQ3_PTK.csv")
sstk_moreThan10LOC<-read.csv("~/treekernel-emse2025/results/RQ3-Size/moreThan10LOC/topk_threshold/metrics_RQ3_SSTK.csv")
stk_moreThan10LOC<-read.csv("~/treekernel-emse2025/results/RQ3-Size/moreThan10LOC/topk_threshold/metrics_RQ3_STK.csv")

ptk_lessThan6LOC$size<-"< 6"
sstk_lessThan6LOC$size<-"< 6"
stk_lessThan6LOC$size<-"< 6"

ptk_moreThan10LOC$size<-"> 10"
sstk_moreThan10LOC$size<-"> 10"
stk_moreThan10LOC$size<-"> 10"

summary(sstk_lessThan6LOC$Prec.5)
summary(sstk_moreThan10LOC$Prec.5)
summary(sstk_lessThan6LOC$Prec.10)
summary(sstk_moreThan10LOC$Prec.10)
summary(sstk_lessThan6LOC$MRR)
summary(sstk_moreThan10LOC$MRR)
summary(sstk_lessThan6LOC$MAP)
summary(sstk_moreThan10LOC$MAP)

summary(stk_lessThan6LOC$Prec.5)
summary(stk_moreThan10LOC$Prec.5)
summary(stk_lessThan6LOC$Prec.10)
summary(stk_moreThan10LOC$Prec.10)
summary(stk_lessThan6LOC$MRR)
summary(stk_moreThan10LOC$MRR)
summary(stk_lessThan6LOC$MAP)
summary(stk_moreThan10LOC$MAP)

summary(ptk_lessThan6LOC$Prec.5)
summary(ptk_moreThan10LOC$Prec.5)
summary(ptk_lessThan6LOC$Prec.10)
summary(ptk_moreThan10LOC$Prec.10)
summary(ptk_lessThan6LOC$MRR)
summary(ptk_moreThan10LOC$MRR)
summary(ptk_lessThan6LOC$MAP)
summary(ptk_moreThan10LOC$MAP)


summary(tfidf_low$Prec.5)
summary(tfidf_high$Prec.5)
summary(tfidf_low$Prec.10)
summary(tfidf_high$Prec.10)
summary(tfidf_low$MRR)
summary(tfidf_high$MRR)
summary(tfidf_low$MAP)
summary(tfidf_high$MAP)


tfidf_low<-read.csv("~/treekernel-emse2025/results/RQ3-Size/RQ3_baseline/metrics_elasticsearch_RQ3_lessThan6LOC.csv")
tfidf_low$size<-"< 6"
tfidf_low$kernel<-"TFIDF"
tfidf_high<-read.csv("~/treekernel-emse2025/results/RQ3-Size/RQ3_baseline/metrics_elasticsearch_RQ3_moreThan10LOC.csv")
tfidf_high$size<-"> 10"
tfidf_high$kernel<-"TFIDF"

combined_data <- bind_rows(ptk_lessThan6LOC, sstk_lessThan6LOC, stk_lessThan6LOC, ptk_moreThan10LOC, sstk_moreThan10LOC, stk_moreThan10LOC,tfidf_low,tfidf_high)
long_data <- combined_data %>%
  pivot_longer(cols = c(Prec.5, Prec.10, MRR, MAP), 
               names_to = "Metric", 
               values_to = "Value")


rq3_plots <- ggplot(long_data, aes(x = kernel, y = Value, fill = size)) +
  geom_boxplot() +
  facet_wrap(~ Metric, scales = "free_y") +  # Facet by Metric, adjust y-axis independently for each
  scale_fill_manual(values = c("> 10" = "orange", "< 6" = "pink")) + 
  labs(y = "", x = "", fill = "LOC") +    # Set legend title for "fill" aesthetic
  theme_minimal() +
  theme(
    legend.title = element_text(),           # Customize the legend title appearance
    axis.text.x = element_text(angle = 45, hjust = 1)  
    )

ggsave("~/treekernel-emse2025/results/RQ3-Size/RQ3-LOC-plot-kelp-vs-tfidf.pdf",rq3_plots,width=6,height=5)

