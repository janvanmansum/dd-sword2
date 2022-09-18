#!/usr/bin/env bash
#
# Copyright (C) 2022 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

TEMPDIR=data

echo -n "Creating test directories..."
mkdir -p data/tmp/1/uploads
mkdir -p data/tmp/1/deposits
mkdir -p data/tmp/2/uploads
mkdir -p data/tmp/2/deposits
echo "OK"

echo -n "Pre-creating log..."
touch $TEMPDIR/dd-sword2.log
echo "OK"
