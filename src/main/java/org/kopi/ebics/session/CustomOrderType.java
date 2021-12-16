/*
 * Copyright (c) 2021, Digital Financial, LLC.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1 as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.kopi.ebics.session;

import java.util.Objects;
import org.kopi.ebics.interfaces.EbicsOrderType;

/**
 *
 * @author nsushkin
 */
public class CustomOrderType implements EbicsOrderType {

    private final String code;

    public CustomOrderType(String code) {
        Objects.requireNonNull(code, "CustomOrderType code must not be null");
        this.code = code.toUpperCase();        
    }
    
    /**
     * Get the value of code
     *
     * @return the value of code
     */
    @Override
    public String getCode() {
        return code;
    }

}
