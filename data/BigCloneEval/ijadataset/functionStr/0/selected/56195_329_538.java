public class Test {    private void _init(final FilterConfig config) throws ServletException {
        if (this.init) {
            return;
        }
        ALCWebFilter.handle = this;
        try {
            try {
                URL url = config.getServletContext().getResource("/WEB-INF/classes/log4j.properties");
                URLConnection uc = url.openConnection();
                lastLoadLog4j = uc.getLastModified();
                try {
                    uc.getInputStream().close();
                } catch (Exception ignore) {
                }
                PropertyConfigurator.configure(url);
            } catch (final Exception e) {
            }
            this.registerConfigMergers();
            final String contextName = this.getContextName(context);
            final StringBuilder bufferUnionAlrFs = new StringBuilder();
            final StringBuilder bufferUnionFs = new StringBuilder();
            final StringBuilder bufferUnionLibsFs = new StringBuilder();
            final FileSystem contextfs = FileSystem.mount(contextName + "_CONTEXT", FileSystemType.SERVLETCONTEXT, context);
            this.listMountedFs.add(contextfs);
            LOG.info("Mounted servlet context filesystem " + contextfs.getLabel());
            try {
                this.lBundlesALCManifest = this.getPackageToDeploy(contextfs.directory("/WEB-INF/bundle-archives/"), context);
            } catch (Exception ignore) {
            }
            LOG.info("lBundlesALCManifest : " + lBundlesALCManifest.size());
            for (final ALCManifest bundle : this.lBundlesALCManifest) {
                LOG.info("mounting : " + bundle.getJarFile().getPath());
                final FileSystem zipfs = FileSystem.mount(contextName + "_ALR_" + bundle.getJarFile().getName(), FileSystemType.ZIP, bundle.getJarFile());
                LOG.info("mounted : " + bundle.getJarFile().getPath());
                bufferUnionAlrFs.append(zipfs.getLabel());
                bufferUnionAlrFs.append(":");
                this.listMountedFs.add(zipfs);
            }
            bufferUnionAlrFs.append(contextfs.getLabel());
            bufferUnionAlrFs.append(":");
            LOG.info("Mounting unionALRFS filesystem " + bufferUnionAlrFs.toString());
            final FileSystem unionALRFS = FileSystem.mount(contextName, FileSystemType.UNION, bufferUnionAlrFs.toString());
            LOG.info("Mounted unionALRFS filesystem " + unionALRFS.getLabel() + " - " + bufferUnionAlrFs.toString());
            final FileSystem memoryfs = FileSystem.mount(unionALRFS.getLabel() + "_MEMORY", FileSystemType.MEMORY, null);
            this.listMountedFs.add(memoryfs);
            LOG.info("Mounted MEMORY filesystem " + memoryfs.getLabel());
            bufferUnionFs.append(unionALRFS.getLabel());
            bufferUnionFs.append(":");
            bufferUnionFs.append(memoryfs.getLabel());
            final Directory archives = unionALRFS.directory("/WEB-INF/archives/");
            this.lALCManifest = this.getPackageToDeploy(archives, context);
            for (int i = this.lALCManifest.size() - 1; i >= 0; i--) {
                final FileSystemElement file = this.lALCManifest.get(i).getJarFile();
                FileSystem zipfs = null;
                try {
                    zipfs = file.isFile() ? FileSystem.mount(unionALRFS.getLabel() + "_" + file.getName(), FileSystemType.ZIP, file) : FileSystem.mount(unionALRFS.getLabel() + "_" + file.getName(), FileSystemType.CHROOT, file);
                } catch (final Exception ignore) {
                    zipfs = FileSystem.getFileSystem(unionALRFS.getLabel() + "_" + file.getName());
                    if (zipfs != null) {
                        zipfs.umount();
                        zipfs = file.isFile() ? FileSystem.mount(unionALRFS.getLabel() + "_" + file.getName(), FileSystemType.ZIP, file) : FileSystem.mount(unionALRFS.getLabel() + "_" + file.getName(), FileSystemType.CHROOT, file);
                    } else {
                        continue;
                    }
                }
                this.listMountedFs.add(zipfs);
                try {
                    FileSystem libs = null;
                    try {
                        libs = FileSystem.mount(unionALRFS.getLabel() + "_" + file.getName() + "_libs", FileSystemType.CHROOT, unionALRFS.getLabel() + "_" + file.getName() + ":/lib/");
                    } catch (final Exception ignore) {
                        libs = FileSystem.getFileSystem(unionALRFS.getLabel() + "_" + file.getName() + "_libs");
                        if (libs != null) {
                            libs.umount();
                            libs = FileSystem.mount(unionALRFS.getLabel() + "_" + file.getName() + "_libs", FileSystemType.CHROOT, unionALRFS.getLabel() + "_" + file.getName() + ":/lib/");
                        }
                    }
                    if (libs != null) {
                        this.listMountedFs.add(libs);
                        LOG.info("Mounted libs filesystem " + unionALRFS.getLabel() + "_" + file.getName() + ":/lib/");
                        if (bufferUnionLibsFs.length() > 0) {
                            bufferUnionLibsFs.append(":");
                        }
                        bufferUnionLibsFs.append(unionALRFS.getLabel());
                        bufferUnionLibsFs.append("_");
                        bufferUnionLibsFs.append(file.getName());
                        bufferUnionLibsFs.append("_libs");
                        for (final File lib : libs.getRoot().getFiles()) {
                            if (lib.getName().endsWith(".jar")) {
                                FileSystem fs = null;
                                try {
                                    fs = FileSystem.mount(unionALRFS.getLabel() + "_" + file.getName() + "_lib_" + lib.getName(), FileSystemType.ZIP, lib);
                                } catch (final Exception ignore) {
                                    fs = FileSystem.getFileSystem(unionALRFS.getLabel() + "_" + file.getName() + "_lib_" + lib.getName());
                                    if (fs != null) {
                                        fs.umount();
                                        fs = FileSystem.mount(unionALRFS.getLabel() + "_" + file.getName() + "_lib_" + lib.getName(), FileSystemType.ZIP, lib);
                                    }
                                }
                                if (fs != null) {
                                    this.listMountedFs.add(fs);
                                    if (bufferUnionLibsFs.length() > 0) {
                                        bufferUnionLibsFs.append(":");
                                    }
                                    bufferUnionLibsFs.append(fs.getLabel());
                                    LOG.info("Mounted lib filesystem " + fs.getLabel());
                                }
                            }
                        }
                    }
                } catch (final IOException ignore) {
                    ;
                }
                try {
                    FileSystem fs = null;
                    try {
                        fs = FileSystem.mount(unionALRFS.getLabel() + "_" + file.getName() + "_webapp", FileSystemType.CHROOT, unionALRFS.getLabel() + "_" + file.getName() + ":/content/");
                    } catch (final Exception ignore) {
                        fs = FileSystem.getFileSystem(unionALRFS.getLabel() + "_" + file.getName() + "_webapp");
                        if (fs != null) {
                            fs.umount();
                            fs = FileSystem.mount(unionALRFS.getLabel() + "_" + file.getName() + "_webapp", FileSystemType.CHROOT, unionALRFS.getLabel() + "_" + file.getName() + ":/content/");
                        }
                    }
                    if (fs != null) {
                        this.listMountedFs.add(fs);
                        LOG.info("Mounted content filesystem " + fs.getLabel() + "_" + file.getName() + ":/content/");
                    }
                    bufferUnionFs.append(":");
                    bufferUnionFs.append(unionALRFS.getLabel());
                    bufferUnionFs.append("_");
                    bufferUnionFs.append(file.getName());
                    bufferUnionFs.append("_webapp");
                } catch (final IOException ignore) {
                    ;
                }
            }
            for (int i = 0; i < this.lALCManifest.size(); i++) {
                final FileSystemElement file = this.lALCManifest.get(i).getJarFile();
                try {
                    FileSystem fs = null;
                    try {
                        fs = FileSystem.mount(unionALRFS.getLabel() + "_" + file.getName() + "_config", FileSystemType.CHROOT, unionALRFS.getLabel() + "_" + file.getName() + ":/config/");
                    } catch (final Exception ignore) {
                        fs = FileSystem.getFileSystem(unionALRFS.getLabel() + "_" + file.getName() + "_config");
                        if (fs != null) {
                            fs.umount();
                            fs = FileSystem.mount(unionALRFS.getLabel() + "_" + file.getName() + "_config", FileSystemType.CHROOT, unionALRFS.getLabel() + "_" + file.getName() + ":/config/");
                        }
                    }
                    if (fs != null) {
                        this.listMountedFs.add(fs);
                        for (final File f : fs.getRoot().getFiles()) {
                            try {
                                ConfigMergerImpl.getInstance().mergeConfig(f, memoryfs, file.getName());
                            } catch (final Exception e) {
                                e.printStackTrace();
                            }
                        }
                        LOG.info("Mounted config filesystem " + fs.getLabel() + "_" + file.getName() + ":/config/");
                    }
                } catch (final IOException ignore) {
                    ;
                }
            }
            FileSystem unionWebappFS = null;
            try {
                unionWebappFS = FileSystem.mount(unionALRFS.getLabel() + "_UNION_webapp", FileSystemType.UNION, bufferUnionFs.toString());
            } catch (final Exception ignore) {
                unionWebappFS = FileSystem.getFileSystem(unionALRFS.getLabel() + "_UNION_webapp");
                if (unionWebappFS != null) {
                    unionWebappFS.umount();
                    unionWebappFS = FileSystem.mount(unionALRFS.getLabel() + "_UNION_webapp", FileSystemType.UNION, bufferUnionFs.toString());
                }
            }
            if (unionWebappFS != null) {
                this.listMountedFs.add(unionWebappFS);
            }
            LOG.info("Mounted lib unionWebappFS " + bufferUnionFs.toString());
            FileSystem unionLibsFS = null;
            try {
                unionLibsFS = FileSystem.mount(unionALRFS.getLabel() + "_UNION_libs", FileSystemType.UNION, bufferUnionLibsFs.toString());
            } catch (final Exception ignore) {
                unionLibsFS = FileSystem.getFileSystem(unionALRFS.getLabel() + "_UNION_libs");
                if (unionLibsFS != null) {
                    unionLibsFS.umount();
                    unionLibsFS = FileSystem.mount(unionALRFS.getLabel() + "_UNION_libs", FileSystemType.UNION, bufferUnionLibsFs.toString());
                }
            }
            if (unionLibsFS != null) {
                this.listMountedFs.add(unionLibsFS);
            }
            LOG.info("Mounted lib unionLibsFS " + bufferUnionLibsFs.toString());
            ALCWebFilter.fsClassLoader = new FileSystemClassLoader(unionLibsFS.getLabel(), bufferUnionLibsFs.toString(), null, this.getClass().getClassLoader());
            LOG.info("Classloader initialized : " + ALCWebFilter.fsClassLoader);
            try {
                this.mainFilter = Dynamic._.Proxy(ALCWebFilter.fsClassLoader.loadClass("org.allcolor.ywt.filter.CMainFilter").newInstance(), MainFilter.class);
                this.mainFilter.init(unionWebappFS, unionALRFS.getLabel());
                LOG.info("MainFilter initialized : " + this.mainFilter);
            } catch (final IllegalAccessException e) {
                throw e;
            } catch (final InstantiationException e) {
                throw e;
            } catch (final ClassNotFoundException e) {
                throw e;
            }
        } catch (final Exception e) {
            throw new ServletException(e);
        }
    }
}