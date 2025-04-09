#RQ3 using threshold and topdocs filter criteria
library(dplyr)
library(ggplot2)   
library(tidyr)
stk_low<-read.csv("~/treekernel-emse2025/results/RQ3-Complexity/result-kelp/LowComplexity/metrics_rq3_STK_topk_threshold.csv")
ptk_low<-read.csv("~/treekernel-emse2025/results/RQ3-Complexity/result-kelp/LowComplexity/metrics_rq3_PTK_topk_threshold.csv")
sstk_low<-read.csv("~/treekernel-emse2025/results/RQ3-Complexity/result-kelp/LowComplexity/metrics_rq3_SSTK_topk_threshold_Less_Complexity.csv")

stk_high<-read.csv("~/treekernel-emse2025/results/RQ3-Complexity/result-kelp/HighComplexity/metrics_rq3_STK_topk_threshold.csv")
ptk_high<-read.csv("~/treekernel-emse2025/results/RQ3-Complexity/result-kelp/HighComplexity/metrics_rq3_PTK_topk_threshold.csv")
sstk_high<-read.csv("~/treekernel-emse2025/results/RQ3-Complexity/result-kelp/HighComplexity/metrics_rq3_SSTK_topk_threshold.csv")
stk_high <- stk_high %>% select(-server)

stk_low$complexity<-"Low"
ptk_low$complexity<-"Low"
sstk_low$complexity<-"Low"

stk_high$complexity<-"High"
ptk_high$complexity<-"High"
sstk_high$complexity<-"High"

summary(sstk_low$Prec.5)
summary(sstk_high$Prec.5)

summary(sstk_low$Prec.10)
summary(sstk_high$Prec.10)

summary(sstk_low$MRR)
summary(sstk_high$MRR)

summary(sstk_low$MAP)
summary(sstk_high$MAP)

tfidf_low<-read.csv("~/treekernel-emse2025/results/RQ3-Complexity/result-baseline/LowComplexity/metrics_elasticsearch_RQ3_low_complexity.csv")
tfidf_low$complexity<-"Low"

tfidf_high<-read.csv("~/treekernel-emse2025/results/RQ3-Complexity/result-baseline/HighComplexity/metrics_elasticsearch_RQ3_high_complexity.csv")
tfidf_high$complexity<-"High"

summary(tfidf_low$Prec.5)
summary(tfidf_high$Prec.5)
summary(tfidf_low$Prec.10)
summary(tfidf_high$Prec.10)
summary(tfidf_low$MRR)
summary(tfidf_high$MRR)
summary(tfidf_low$MAP)
summary(tfidf_high$MAP)


combined_data <- bind_rows(ptk_low, sstk_low, stk_low, ptk_high, sstk_high, stk_high, tfidf_low, tfidf_high)
summary(combined_data[combined_data$complexity=="Low",]$Prec.5)
summary(combined_data[combined_data$complexity=="High",]$Prec.5)

long_data <- combined_data %>%
             pivot_longer(cols = c(Prec.5, Prec.10, MRR, MAP), 
               names_to = "Metric", 
               values_to = "Value")
long_data$complexity <- factor(long_data$complexity, levels = c("Low", "High"))
rq3_plots <- ggplot(long_data, aes(x = kernel, y = Value, fill = complexity)) +
  geom_boxplot() +
  facet_wrap(~ Metric, scales = "free_y") +  # Facet by Metric, adjust y-axis independently for each
  scale_fill_manual(values = c("High" = "lightblue", "Low" = "green")) + 
  labs(y = "", x = "", fill = "Complexity") +    # Set legend title for "fill" aesthetic
  theme_minimal() +
  theme(
    legend.title = element_text(),           # Customize the legend title appearance
    axis.text.x = element_text(angle = 45, hjust = 1)  
  )

ggsave("~/treekernel-emse2025/results/RQ3-Complexity/RQ3-complexity-plot-kelp-vs-tfidf.pdf",rq3_plots,width=6,height=5)


