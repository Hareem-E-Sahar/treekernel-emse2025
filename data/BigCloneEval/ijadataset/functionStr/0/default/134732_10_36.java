public class Test {    @Override
    public boolean rawInvocation(Request request, Response response) throws Exception {
        if ("/@api".equals(request.path) || "/@api/".equals(request.path)) {
            response.status = 302;
            response.setHeader("Location", "/@api/index.html");
            return true;
        }
        if (request.path.startsWith("/@api/")) {
            if (request.path.matches("/@api/-[a-z]+/.*")) {
                String module = request.path.substring(request.path.indexOf("-") + 1);
                module = module.substring(0, module.indexOf("/"));
                VirtualFile f = Play.modules.get(module).child("documentation/api/" + request.path.substring(8 + module.length()));
                if (f.exists()) {
                    response.contentType = MimeTypes.getMimeType(f.getName());
                    response.out.write(f.content());
                }
                return true;
            }
            File f = new File(Play.frameworkPath, "documentation/api/" + request.path.substring(6));
            if (f.exists()) {
                response.contentType = MimeTypes.getMimeType(f.getName());
                response.out.write(IO.readContent(f));
            }
            return true;
        }
        return false;
    }
}