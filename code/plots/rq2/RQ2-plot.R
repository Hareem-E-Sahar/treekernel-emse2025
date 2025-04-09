library(dplyr)
library(ggplot2)
library(tidyr)

source("./RQ2-threshold.R")
source("./RQ2-topk.R")
source("./RQ2-topk-threshold.R")

# Bind all dataframes together
rq2_topk     <-bind_rows(ptkT1, ptkT2, ptkT3, sstkT1, sstkT2, sstkT3, stkT1, stkT2, stkT3)
rq2_threshold<-bind_rows(ptkT1_threshold, ptkT2_threshold, ptkT3_threshold, sstkT1_threshold, sstkT2_threshold, sstkT3_threshold, stkT1_threshold, stkT2_threshold, stkT3_threshold)
rq2_topk_threshold <- bind_rows(ptkT1_topk_threshold, ptkT2_topk_threshold, ptkT3_topk_threshold, sstkT1_topk_threshold, sstkT2_topk_threshold, sstkT3_topk_threshold, stkT1_topk_threshold, stkT2_topk_threshold, stkT3_topk_threshold)


combined_typedata_kernels <- bind_rows(rq2_topk,rq2_threshold,rq2_topk_threshold)

#reshape the data
long_data <- combined_typedata_kernels %>%
  pivot_longer(cols = c(`Prec@5`, `Prec@10`, MRR, MAP), 
               names_to = "Metric", 
               values_to = "Value")

result <- long_data %>%
  group_by(kernel, filter, type, Metric) %>%
  summarise(Mean_Value = mean(Value, na.rm = TRUE), .groups = "drop")

write.csv(result, "./RQ2-summary.csv", row.names = FALSE)

rq2plot_by_kernel <- ggplot(long_data, aes(x = type, y = Value, fill = kernel)) +
  geom_boxplot() +
  facet_wrap(~ Metric, scales = "free_y") +  # Facet by Metric
  labs(  x = "",y = "") +
  scale_fill_brewer(palette = "Set2") +  # Choose a color palette
  theme_minimal() +
  theme(legend.title = element_blank())

rq2plot_by_filter <- ggplot(long_data, aes(x = type, y = Value, fill = filter)) +  # Use filter in fill aesthetic
  geom_boxplot() +
  facet_wrap(~ Metric, scales = "free_y") +  # Facet by Metric
  labs(x = "", y = "") +
  scale_fill_brewer(palette = "Set2") +  # Choose a color palette
  theme_minimal() +
  theme(legend.title = element_blank())



nicad<-read.csv("~/treekernel-emse2025/results/RQ2/metrics_nicad_typewise.csv")
nicad$kernel<-"Nicad"
colnames(nicad)<- c("type","Prec@5","Prec@10","MRR","MAP","Recall","seed","run","kernel","deterministic","subtype")
nicad<- nicad %>%
  select(-deterministic,-subtype)
filtered_df <- nicad %>% filter(run == 3)
nicad_type1 <- filtered_df %>% filter(type == "T1")
nicad_type2 <- filtered_df %>% filter(type == "T2")
nicad_type3 <- filtered_df %>% filter(type == "T3")


combined_typedata <- bind_rows(combined_typedata_kernels,nicad_type1,nicad_type2,nicad_type3)
#reshape the data
long_data_nicad <- combined_typedata %>%
  pivot_longer(cols = c(`Prec@5` ,`Prec@10`, MRR, MAP), 
               names_to = "Metric", 
               values_to = "Value")


rq2plot_nicad <- ggplot(long_data_nicad, aes(x = type, y = Value, fill = kernel)) +
  geom_boxplot() +
  facet_wrap(~ Metric, scales = "free_y") +  # Facet by Metric
  labs(  x = "",y = "") +
  scale_fill_brewer(palette = "Set3") +  # Choose a color palette
  theme_minimal() +
  theme(legend.title = element_blank())

tfidf<-read.csv("~/treekernel-emse2025/results/RQ2/metrics-elastic-rq2-type-specific-refclones.csv")
colnames(tfidf)<-c("Prec@5","Prec@10",	"MRR","MAP","technique","functionality","time","seed","type")
tfidf$kernel<-"TF-IDF"
combined_typedata_all <- bind_rows(combined_typedata_kernels,nicad_type1,nicad_type2,nicad_type3,tfidf)
#reshape the data
long_data_nicad_tfidf <- combined_typedata_all %>%
  pivot_longer(cols = c(`Prec@5` ,`Prec@10`, MRR, MAP), 
               names_to = "Metric", 
               values_to = "Value")


rq2plot_nicad_tfidf <- ggplot(long_data_nicad_tfidf, aes(x = type, y = Value, fill = kernel)) +
  geom_boxplot() +
  facet_wrap(~ Metric, scales = "free_y") +  # Facet by Metric
  labs(  x = "",y = "") +
  scale_fill_manual(values = c("PTK" = "pink", "SSTK" = "lightblue", 
                               "STK" = "lightgreen", "Nicad" = "purple",
                               "TF-IDF"="orange" ))+
  theme_minimal() +
  theme(legend.title = element_blank(),legend.position = "top")

ggsave("./rq2-all.pdf",rq2plot_nicad_tfidf,width=5.5,height=6)
  