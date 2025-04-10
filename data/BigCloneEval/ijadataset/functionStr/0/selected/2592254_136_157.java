public class Test {    public void handleDeletePosts(HttpServletRequest request, HttpServletResponse response) throws NGFException {
        logger.info("handleDeleteThread");
        String threadid = request.getParameter("threadid");
        System.out.println("handleDeleteThread  post id = " + threadid);
        Posts posts = new Posts();
        ThreadPostBL threadPostBL = new ThreadPostBL();
        try {
            logger.debug("start try handleDeleteThread = " + threadid);
            posts.setPostId(Integer.parseInt(threadid));
            threadPostBL.deletePost(posts);
            this.initAction(request, response);
            this.writeResponse("<message>Thread Deletes </message>");
        } catch (NGFException e) {
            this.initAction(request, response);
            this.writeResponse("<error>" + e.getErrorMessage() + "</error>");
            System.out.print("Search Forum Excepton = " + e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage());
            this.initAction(request, response);
            this.writeResponse("<error>" + e.getMessage() + "</error>");
        }
    }
}