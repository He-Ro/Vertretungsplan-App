#! /usr/bin/env python3
""" Get the download count of a given tag """

import argparse
import json
import urllib.request


def parse():
    """ Parse the command line arguments """
    parser = argparse.ArgumentParser()
    parser.add_argument('tag', help='The tag, for which the '
                        'download count should be reported')
    args = parser.parse_args()
    return args


def main():
    """ The main function """
    args = parse()
    base_url = 'https://api.github.com/repos/He-Ro/' \
               'Vertretungsplan-App/releases/tags/'
    try:
        with urllib.request.urlopen(base_url + args.tag) as response:
            tag_info = json.loads(response.read())
            author = tag_info['author']['login']
            date_published = tag_info['published_at']
            apk_found = False
            for asset in tag_info['assets']:
                if (asset['content_type'] == 'application/'
                                             'vnd.android.package-archive'):
                    if apk_found:
                        raise ValueError('There are multiple APKs in '
                                         'this release')
                    apk_found = True
                    apk_name = asset['name']
                    apk_download_count = int(asset['download_count'])

            if not apk_found:
                raise ValueError('There does not seem to be an APK '
                                 'for release %s', args.tag)

            print("Release {}\nAuthored by {} on {}".format(args.tag, author,
                                                            date_published))
            print("APK file {}\nDownloads: {}".format(apk_name,
                                                      apk_download_count))
    except urllib.error.HTTPError:
        raise ValueError('The tag {} does not seem to exist'.format(args.tag))


if __name__ == '__main__':
    main()
