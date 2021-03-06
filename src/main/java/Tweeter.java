import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;

public class Tweeter {
    Twitter twitter = TwitterFactory.getSingleton();

    void postTweet(String content, Configuration cb) throws TwitterException {


        TwitterFactory tf = new TwitterFactory(cb);
        Twitter twitter = tf.getInstance();

        Status status = twitter.updateStatus(content);
        System.out.println("Successfully updated the status to [" + status.getText() + "].");
    }
}
