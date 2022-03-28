#!/bin/bash

for hash in $(git verify-pack -v .git/objects/pack/*.idx | grep blob | sort -k 3 -n -r | awk '{print$1}'); do
    filename=$(git rev-list --objects --all | grep ${hash} | awk '{print$2}' | grep '^wallpaper.*jpg$')
    if [[ "$filename" != "" ]]; then
        if [[ $(git log --pretty=oneline --branches -- ${filename} | wc -l) -eq 2 ]]; then
            git filter-branch --force --index-filter "git rm --cached --ignore-unmatch ${filename}" --prune-empty --tag-name-filter cat -- --all
        fi
        
    fi
done

git push origin master --force
git for-each-ref --format='delete %(refname)' refs/original | git update-ref --stdin
git reflog expire --expire=now --all
git gc --prune=now
