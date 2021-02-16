import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class Tweeter {
    Twitter twitter = TwitterFactory.getSingleton();

    void postTweet(String content, ConfigurationBuilder cb) throws TwitterException {
//        ConfigurationBuilder cb = new ConfigurationBuilder();
//        cb.setDebugEnabled(true)
//                .setOAuthConsumerKey(args[0])
//                .setOAuthConsumerSecret("******************************************")
//                .setOAuthAccessToken("**************************************************")
//                .setOAuthAccessTokenSecret("******************************************");
        TwitterFactory tf = new TwitterFactory(cb.build());
        Twitter twitter = tf.getInstance();

        Status status = twitter.updateStatus(content);
        System.out.println("Successfully updated the status to [" + status.getText() + "].");
    }
}
