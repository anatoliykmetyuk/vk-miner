# VK Miner
This application allows you to map out our social circle or a community of the [VK](https://vk.com) network. It creates a `*.gexf` graph files that can be opened by [Gephi](https://gephi.org/).

## Prerequisites
1. [Gephi](https://gephi.org/). Note that it runs properly only on Java 6. It won't run on higher versions.
2. Java 6

## Usage
1. Download the latest release and unzip it
2. Run `vk-miner.sh` (Linux and Mac) or `vk-miner.bat` (Windows)
3. In the `Id` field, enter an id of your target person or community. For example, in `https://vk.com/durov` profile URL, `durov` is the profile id. Note that if you want to mine a community and its url is like `https://vk.com/club123456`, the id is `123456`, not `club123456`.
4. If you want a detailed analysis of the wall of each discovered user, put the `Wall analysis` flag. Wall analysis allows to discover users that may not be in the friend list of the target user, but still left traces on his wall (comments, likes, shares). Also, the more traces the user left on the wall, the stronger is the connection between him and the owner of the wall - it is reflected in the resulting graph.
5. Press `Select` button and choose where to save the resulting graph.
6. Press `Person` button if the id you've entered belongs to a user's profile; press `Community` if it is a community.
7. After some time, `*.gexf` graf file will be created. You can open it in Gephi. See Gephi's official site to learn how to use it.