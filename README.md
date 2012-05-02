
This demo project shows how jclouds, whirr, and brooklyn help build and manage Hadoop clusters.

Run  `mvn clean install`  to download dependencies and compile the code.


## JCLOUDS

Run `demo.jclouds.CreateMachine` to create (two) machines in AWS (or other cloud).
Set `-D` arguments as described in its javadoc.

This displays a private key which you can copy to a file (e.g. `/tmp/key`)
and the IPs you can ssh to (e.g. `ssh -i /tmp/key user@host`).

Alternatively you can run:

    https://github.com/jclouds/jclouds-examples/blob/master/compute-basics/
        src/main/java/org/jclouds/examples/compute/basics/MainApp.java
    

On one of these machines you can:

    `curl -O http://mirrors.ibiblio.org/apache/hadoop/common/hadoop-1.0.2/hadoop-1.0.2-bin.tar.gz`

Untar this, put some text into `/tmp/sample.txt`, set `JAVA_HOME`, then in the root of the `hadoop-1.0.2` run:

    `bin/hadoop jar ./hadoop-examples-1.0.2.jar wordcount /tmp/sample.txt /tmp/out`

This shows hadoop being run on a cloud machine.
To run it "properly", distributed, is a bit more involved,
setting up conf files etc.
This is described at `http://hadoop.apache.org/common/docs/current/cluster_setup.html`.
Or it can be automated with...


## WHIRR

Set up the following (or comparable in your IDE):

{% highlight bash %}
export CLOUD_ID=AKAIyourIDhere 
export CLOUD_SECRET=secretCredentialFromThem
{% endhighlight %}

Then run the `demo.whirr.WhirrHadoop` class or use the command-line:

{% highlight bash %}
curl -O http://www.apache.org/dist/whirr/whirr-0.7.1/whirr-0.7.1.tar.gz
tar zxf whirr-0.7.1.tar.gz; cd whirr-0.7.1 

bin/whirr launch-cluster --config /path/to/this/project/src/main/resources/whirr-hadoop.properties 
{% endhighlight %}

In a few minutes you should see messages like this:

{% highlight %}
12/05/01 17:36:35 INFO hadoop.HadoopJobTrackerClusterActionHandler: Completed configuration of whirr-hadoop-sample-alex role hadoop-jobtracker
12/05/01 17:36:35 INFO hadoop.HadoopJobTrackerClusterActionHandler: Jobtracker web UI available at http://ec2-184-169-225-252.us-west-1.compute.amazonaws.com:50030
12/05/01 17:36:35 INFO hadoop.HadoopNameNodeClusterActionHandler: Completed configuration of whirr-hadoop-sample-alex role hadoop-namenode
12/05/01 17:36:35 INFO hadoop.HadoopNameNodeClusterActionHandler: Namenode web UI available at http://ec2-184-169-225-252.us-west-1.compute.amazonaws.com:50070

12/05/01 17:36:35 INFO hadoop.HadoopConfigurationConverter: Wrote file /Users/alex/.whirr/whirr-hadoop-sample-alex/hadoop-site.xml
12/05/01 17:36:35 INFO hadoop.HadoopNameNodeClusterActionHandler: Wrote Hadoop proxy script /Users/alex/.whirr/whirr-hadoop-sample-alex/hadoop-proxy.sh

12/05/01 17:36:35 INFO hadoop.HadoopDataNodeClusterActionHandler: Completed configuration of whirr-hadoop-sample-alex role hadoop-datanode
12/05/01 17:36:35 INFO hadoop.HadoopTaskTrackerClusterActionHandler: Completed configuration of whirr-hadoop-sample-alex role hadoop-tasktracker
12/05/01 17:36:35 INFO actions.ScriptBasedClusterAction: Finished running start phase scripts on all cluster instances
...

Your cluster is ready.
{% endhighlight %}

Visit the jobtracker/namenode on in a browser on port 50030/50070.

Now we'll run a hadoop process against this:
* First we need to run the proxy script noted in the output from whirr (because Hadoop binds only to an internal port)
* Next build a JAR for this project, e.g. `cd target/classes ; jar cvf /tmp/jar-for-my-hadoop-app.jar .`
* Next open, configure, and run `demo.whirr.WhirrWordCount`
  * configure it to use the *JAR* you just made, ahead of any directory e.g. `target/classes` in the classpath
    (this is required because Hadoop detects the JAR from which a class comes
    and passes it to the server, where the Mapper code is required)
  * create (optionally) a file /tmp/sample.txt
  * look at the HadoopConfiguration (hadoop-site.xml) noted in the output from whirr 
    (the app is hard-coded to read this)
  * run it


## BROOKLYN

Put your IDs and credentials into `~/.brooklyn/brooklyn.properties` as follows:

{% highlight %}
brooklyn.jclouds.aws-ec2.identity=AKAIyourIDhere
brooklyn.jclouds.aws-ec2.credential=secretCredentialFromThem

brooklyn.geoscaling.username=YourGeoscalingAccount
brooklyn.geoscaling.password=y0urG30SC4L1NGp455w0rd
brooklyn.geoscaling.primaryDomain=yourname.geopaas.org
{% endhighlight %}

(See `http://brooklyncentral.github.com/use/examples/global-web-fabric/` for more instructions,
including setting up Geoscaling.)

Then edit (e.g. locations) and run the `demo.brooklyn.BrooklynWebFabricWithHadoop` example 
(or `examples/hadoop-and-whirr` in `https://github.com/brooklyncentral/brooklyn`).
Make sure to run with `-Xmx256m -Xmx1g -XX:MaxPermSize=256m`.

CORRECTION: Currently the Brooklyn example must be run from brooklyncentral/brooklyn examples
