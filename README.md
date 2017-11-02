# emacs-maven-plugin

* Description

Maven plugin that generates a .dir-locals.el file with (hopefully) useful information (currently classpath)

* Howto

As the plugin is not listed on any public server you have to install it into your local
repostiroy first:

$ mvn install

Then add a dependency in your java project:

      <plugin>
        <groupId>emacs.helper</groupId>
        <artifactId>emacs-maven-plugin</artifactId>
        <version>1.2-SNAPSHOT</version>
        <executions>
          <execution>
            <phase>
              verify
            </phase>
              <goals>
                <goal>dirlocals</goal>
              </goals>
          </execution>
        </executions>
      </plugin>

Finally call

$ mvn verify

to have the .dir-locals.el file generated.

Now if the .dir-locals.el file is read by emacs it will provide two variables

1. java-classpath with a list of jar files from the projects compile time classpath
2. java-project-root pointing to the projects root directory

The class path can then be used to do handy things. I have added a source for auto-complete package like this:

(defvar-local java-classpath nil "Java classpath. This will be set by .dir-locals.el (hopefully).")
(defvar-local java-project-root nil "Buffer local location of current project root.")
(defvar-local java-classes-cache nil "Cache for the current classpath classes.")
(defvar jdk-location "/path/to/jdk")

(defun java-read-classes-from-classpath ()
  "Iterate over classpath and gather classes from jar files.
Evaluates into one large list containing all classes."
  (let* ((jarfiles nil)
         (jarfile nil)
         (result '()))
    (progn
      (dolist (file (directory-files (concat jdk-location "jre/lib/") t "\.\*.jar\$"))
        (setq jarfiles (cons file jarfiles)))
      (dolist (file (reverse java-classpath))
        (setq jarfiles (cons file jarfiles))))
    (with-temp-buffer
      (while jarfiles
        (progn
          (setq jarfile (car jarfiles)
                jarfiles (cdr jarfiles))
          (call-process "/usr/bin/unzip" nil t nil "-l" (expand-file-name jarfile))
          (goto-char (point-min))
          (let ((end 0)
                (classname ""))
            (while (search-forward ".class" nil t nil)
              (end-of-line)
              (setq end (point))
              (beginning-of-line)
              (goto-char (+ (point) 30))
              (setq classname (substring 
                               (replace-regexp-in-string "/" "."
                                                         (buffer-substring-no-properties (point) end))
                               0 -6))
              (setq result (cons classname result))
              (forward-line 1)
              (beginning-of-line))
            (erase-buffer)))))
    result))

(defvar ac-source-classpath-cache nil)

(defun ac-source-classpath-init ()
   (setq ac-source-classpath-cache java-read-classes-from-classpath))

(defvar ac-source-classpath
    '((init . ac-source-classpath-init)
      (candidates . ac-source-classpath-cache)
      (prefix . "^import \\(.*\\)")))

* History

* History

Version 1.2: Using getClasspathElements()
