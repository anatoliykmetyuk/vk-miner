# VK Miner
This application allows you to map out our social circle or a community of the [VK](https://vk.com) network. It creates `*.gexf` graph files that can be opened by [Gephi](https://gephi.org/).

## Prerequisites
1. [Gephi](https://gephi.org/)
2. [Java Runtime Environment (JRE) 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html)

## Usage
1. [Download](https://github.com/anatoliykmetyuk/vk-miner/releases/download/v1.0.1/vk-miner-1.0.1.zip) the latest release and unzip it (**it won't run unless unzipped!**)
2. Run `vk-miner.sh` (Linux and Mac) or `vk-miner.bat` (Windows)
3. In the `Id` field, enter an id of your target person or community. For example, in `https://vk.com/id1234567` profile URL, `id1234567` is the profile id. Note that if you want to mine a community and its url is like `https://vk.com/club123456`, the id is `123456`, not `club123456`.
4. If you want a detailed analysis of the wall of each discovered user, put the `Wall analysis` flag. Wall analysis allows to discover users that may not be in the friend list of the target user, but still left traces on his wall (comments, likes, shares). Also, the more traces the user left on the wall, the stronger is the connection between him and the owner of the wall - it is reflected in the resulting graph.
5. Press `Select` button and choose where to save the resulting graph.
6. Press `Person` button if the id you've entered belongs to a user's profile; press `Community` if it is a community.
7. After some time, `*.gexf` graf file will be created. You can open it in Gephi. See Gephi's official site to learn how to use it.

## Screenshots
![screen](http://s14.postimg.org/iize1p65d/Screen_Shot_2016_01_26_at_18_50_26.png)
![screen](http://s10.postimg.org/fnvuupw09/image.png)