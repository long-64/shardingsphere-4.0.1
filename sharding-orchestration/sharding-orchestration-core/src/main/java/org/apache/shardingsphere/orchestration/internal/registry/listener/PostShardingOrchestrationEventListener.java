/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.orchestration.internal.registry.listener;

import com.google.common.eventbus.EventBus;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.orchestration.internal.eventbus.ShardingOrchestrationEventBus;
import org.apache.shardingsphere.orchestration.reg.api.RegistryCenter;
import org.apache.shardingsphere.orchestration.reg.listener.DataChangedEvent;
import org.apache.shardingsphere.orchestration.reg.listener.DataChangedEvent.ChangedType;
import org.apache.shardingsphere.orchestration.reg.listener.DataChangedEventListener;

import java.util.Arrays;
import java.util.Collection;

/**
 * Post sharding orchestration event listener.
 *
 * @author zhangliang
 * @author panjuan
 */
@RequiredArgsConstructor
public abstract class PostShardingOrchestrationEventListener implements ShardingOrchestrationListener {

    // 创建 EventBus, 单例模式
    private final EventBus eventBus = ShardingOrchestrationEventBus.getInstance();
    
    private final RegistryCenter regCenter;
    
    private final String watchKey;
    
    @Override
    public final void watch(final ChangedType... watchedChangedTypes) {
        final Collection<ChangedType> watchedChangedTypeList = Arrays.asList(watchedChangedTypes);
        regCenter.watch(watchKey, new DataChangedEventListener() {
            
            @Override
            public void onChange(final DataChangedEvent dataChangedEvent) {
                if (watchedChangedTypeList.contains(dataChangedEvent.getChangedType())) {
                    eventBus.post(createShardingOrchestrationEvent(dataChangedEvent));
                }
            }
        });
    }
    
    protected abstract ShardingOrchestrationEvent createShardingOrchestrationEvent(DataChangedEvent event);
}
