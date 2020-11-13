package cc.moecraft.icq.accounts;

import cc.moecraft.icq.PicqBotX;
import cc.moecraft.icq.sender.IcqHttpApi;
import com.typesafe.config.ConfigFactory;

import java.util.Objects;

/**
 * 此类由 Hykilpikonna 在 2018/08/25 创建!
 * Created by Hykilpikonna on 2018/08/25!
 * Github: https://github.com/hykilpikonna
 * QQ: admin@moecraft.cc -OR- 871674895
 *
 * @author Hykilpikonna
 */
public class BotAccount
{
    private final String name;

    private final String postUrl;

    private final int postPort;

    private long id;

    private IcqHttpApi httpApi;

    public BotAccount(String name, PicqBotX bot, String postUrl, int postPort)
    {
        this.name = name;
        this.postUrl = postUrl;
        this.postPort = postPort;

        this.httpApi = new IcqHttpApi(bot, this, postUrl, postPort);
        this.id = ConfigFactory.load().getLong("bot.jam_qq");
    }

    public String getName() {
        return name;
    }

    public String getPostUrl() {
        return postUrl;
    }

    public int getPostPort() {
        return postPort;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public IcqHttpApi getHttpApi() {
        return httpApi;
    }

    public void setHttpApi(IcqHttpApi httpApi) {
        this.httpApi = httpApi;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BotAccount)) return false;
        BotAccount that = (BotAccount) o;
        return postPort == that.postPort &&
                id == that.id &&
                Objects.equals(name, that.name) &&
                Objects.equals(postUrl, that.postUrl) &&
                Objects.equals(httpApi, that.httpApi);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, postUrl, postPort, id, httpApi);
    }
}
