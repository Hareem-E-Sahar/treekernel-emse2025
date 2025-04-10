public class Test {    public static void parseSinaStock(ArrayList<String> dataSource, final ArrayList<SinaStockBean> sinaStockBeanList) throws IOReactorException, InterruptedException {
        HttpAsyncClient httpclient = new DefaultHttpAsyncClient();
        httpclient.start();
        if (dataSource != null && dataSource.size() > 0) {
            final CountDownLatch latch = new CountDownLatch(dataSource.size());
            for (int i = 0; i < dataSource.size(); i++) {
                final HttpGet request = new HttpGet(dataSource.get(i));
                httpclient.execute(request, new FutureCallback<HttpResponse>() {

                    public void completed(final HttpResponse response) {
                        System.out.println(" Request completed " + count + " " + request.getRequestLine() + " " + response.getStatusLine());
                        try {
                            HttpEntity he = response.getEntity();
                            try {
                                String resp = EntityUtils.toString(he, "gb2312");
                                if (resp != null && resp.length() > 0) {
                                    SinaStockBean ssb = SinaStockPostProcess.postSinaStockBeanProcess(resp);
                                    sinaStockBeanList.add(ssb);
                                }
                                count++;
                            } catch (ParseException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            latch.countDown();
                        } catch (RuntimeException re) {
                            latch.countDown();
                        }
                    }

                    public void failed(final Exception ex) {
                        latch.countDown();
                    }

                    public void cancelled() {
                        latch.countDown();
                    }
                });
            }
            latch.await();
            System.out.println("done");
        }
        if (httpclient != null) {
            httpclient.shutdown();
        }
        System.out.println(sinaStockBeanList.size());
    }
}